package odk.malicompetences.backend.institution;

import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.besoin.BesoinCompetence;
import odk.malicompetences.backend.besoin.BesoinCompetenceRepository;
import odk.malicompetences.backend.besoin.StatutBesoin;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.NiveauTerritoire;
import odk.malicompetences.backend.territoire.Region;
import odk.malicompetences.backend.territoire.RegionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Statistiques calculées à la demande (consignes V1.2) et confidentialité des petites cellules. */
class InstitutionIndicateurServiceTest {

    private final Competence competence = competence("Soudure");
    private final Region r1 = region("ML-REG-01", "Kayes", NiveauTerritoire.REGION, null);
    private final Region r2 = region("ML-REG-02", "Koulikoro", NiveauTerritoire.REGION, null);
    private final Region r3 = region("ML-REG-03", "Sikasso", NiveauTerritoire.REGION, null);
    private final Region cercleKayes = region("ML-CER-01", "Cercle de Kayes", NiveauTerritoire.CERCLE, r1);

    @Test
    void espaceInstitutionResteDesactiveParDefaut() {
        var calcul = mock(IndicateurCalculService.class);
        var service = new InstitutionIndicateurService(calcul, mock(RegionRepository.class),
                mock(CompetenceRepository.class), new InstitutionFeatureGate(false, false));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.lire(UUID.randomUUID(), null, null));

        assertEquals(503, error.getStatusCode().value());
        verifyNoInteractions(calcul);
    }

    @Test
    void offreEnPersonnesDistinctesDemandeSansDoubleCompteEtEcartOffreMoinsDemande() {
        List<CitoyenCompetence> validees = new ArrayList<>();
        Citoyen a = citoyen(r1);
        validees.add(validee(a, null));
        validees.add(validee(a, null)); // même personne : comptée une seule fois
        validees.add(validee(citoyen(r1), LocalDate.now().minusDays(1))); // validation expirée : exclue
        validees.add(validee(citoyen(null), null)); // sans commune : national seulement

        BesoinCompetence parCommune = besoin(StatutBesoin.OUVERT, null, null, communeDe(r1), 3);
        BesoinCompetence parCercle = besoin(StatutBesoin.OUVERT, null, cercleKayes, null, 2);
        BesoinCompetence sansZone = besoin(StatutBesoin.OUVERT, null, null, null, 4);
        BesoinCompetence expire = besoin(StatutBesoin.OUVERT, LocalDate.now().minusDays(1), r1, null, 10);
        BesoinCompetence brouillon = besoin(StatutBesoin.BROUILLON, null, r1, null, 10);

        var lignes = IndicateurCalculService.agreger(validees,
                List.of(parCommune, parCercle, sansZone, expire, brouillon), LocalDate.now(), null);

        var national = lignes.stream().filter(l -> l.territoire() == null).findFirst().orElseThrow();
        assertEquals(2, national.offre());
        assertEquals(9, national.demande());
        assertEquals(-7, national.ecart());

        var kayes = lignes.stream().filter(l -> l.territoire() == r1).findFirst().orElseThrow();
        assertEquals(1, kayes.offre());
        assertEquals(5, kayes.demande()); // commune + cercle ramené à sa région, sans doublon
    }

    @Test
    void masqueLesCellulesDeUnAQuatrePersonnesEtUneCelluleComplementaire() {
        List<CitoyenCompetence> validees = new ArrayList<>();
        for (int i = 0; i < 3; i++) validees.add(validee(citoyen(r1), null));
        for (int i = 0; i < 6; i++) validees.add(validee(citoyen(r2), null));
        for (int i = 0; i < 9; i++) validees.add(validee(citoyen(r3), null));

        var citoyenCompetences = mock(CitoyenCompetenceRepository.class);
        var besoins = mock(BesoinCompetenceRepository.class);
        when(citoyenCompetences.findByEtat(EtatCompetence.VALIDEE)).thenReturn(validees);
        when(besoins.findByBesoin_Statut(StatutBesoin.OUVERT)).thenReturn(List.of());
        var regions = mock(RegionRepository.class);
        when(regions.findAllByNiveauNot(NiveauTerritoire.CERCLE)).thenReturn(List.of(r1, r2, r3));
        var competences = mock(CompetenceRepository.class);
        when(competences.findById(competence.getId())).thenReturn(Optional.of(competence));

        var service = new InstitutionIndicateurService(new IndicateurCalculService(citoyenCompetences, besoins),
                regions, competences, new InstitutionFeatureGate(true, true));
        var rows = service.lire(competence.getId(), null, null);

        assertEquals("MASQUE", rows.get(0).statutConfidentialite());
        assertNull(rows.get(0).offre());
        assertEquals("MASQUE", rows.get(1).statutConfidentialite()); // suppression complémentaire
        assertEquals("VISIBLE", rows.get(2).statutConfidentialite());
        assertEquals(Integer.valueOf(9), rows.get(2).offre());
        assertEquals(Integer.valueOf(9), rows.get(2).ecart());
    }

    // ------------------------------------------------------------------

    private CitoyenCompetence validee(Citoyen citoyen, LocalDate valideJusquau) {
        CitoyenCompetence cc = new CitoyenCompetence();
        cc.setCitoyen(citoyen);
        cc.setCompetence(competence);
        cc.setEtat(EtatCompetence.VALIDEE);
        cc.setValideJusquau(valideJusquau);
        return cc;
    }

    private BesoinCompetence besoin(StatutBesoin statut, LocalDate dateFin, Region zone, Commune commune, int quantite) {
        Besoin b = new Besoin();
        b.setStatut(statut);
        b.setDateFin(dateFin);
        b.setRegion(zone);
        b.setCommune(commune);
        BesoinCompetence bc = new BesoinCompetence();
        bc.setBesoin(b);
        bc.setCompetence(competence);
        bc.setQuantite(quantite);
        return bc;
    }

    private static Citoyen citoyen(Region region) {
        Citoyen c = new Citoyen();
        c.setId(UUID.randomUUID());
        if (region != null) c.setCommune(communeDe(region));
        return c;
    }

    private static Commune communeDe(Region region) {
        Commune commune = new Commune();
        commune.setId(UUID.randomUUID());
        commune.setNom("Commune");
        commune.setRegion(region);
        return commune;
    }

    private static Region region(String code, String nom, NiveauTerritoire niveau, Region parent) {
        Region r = new Region();
        r.setId(UUID.randomUUID());
        r.setCodeStable(code);
        r.setNom(nom);
        r.setNiveau(niveau);
        r.setParent(parent);
        return r;
    }

    private static Competence competence(String nom) {
        Competence c = new Competence();
        c.setId(UUID.randomUUID());
        c.setNom(nom);
        return c;
    }
}
