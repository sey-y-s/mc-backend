package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.besoin.BesoinCompetenceRepository;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.citoyen.Disponibilite;
import odk.malicompetences.backend.citoyen.ProfilCompletudeService;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.portfolio.PortfolioRealisationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Règles de visibilité : un citoyen ne se trouve pas lui-même, et un profil incomplet ne cherche pas. */
class TalentRechercheRegleTest {

    private final CitoyenRepository citoyens = mock(CitoyenRepository.class);
        private final ProfilCompletudeService completude = mock(ProfilCompletudeService.class);
    private final odk.malicompetences.backend.organisation.OrganisationRepository organisations =
            mock(odk.malicompetences.backend.organisation.OrganisationRepository.class);
    private final odk.malicompetences.backend.miseenrelation.DemandeMiseEnRelationRepository relations =
            mock(odk.malicompetences.backend.miseenrelation.DemandeMiseEnRelationRepository.class);
    private final TalentRechercheService service = new TalentRechercheService(citoyens,
            mock(CitoyenCompetenceRepository.class), mock(BesoinRepository.class),
            mock(BesoinCompetenceRepository.class), mock(PortfolioRealisationRepository.class), completude,
            relations, mock(odk.malicompetences.backend.portfolio.PortfolioRealisationMapper.class), organisations);

    private final Citoyen moi = citoyen();
    private final Citoyen autre = citoyen();
    private final Citoyen invisible = citoyen();
    private final TalentFiltres sansFiltre = new TalentFiltres(null, null, null, null, null, null, null, null, null, null);

    TalentRechercheRegleTest() {
        when(citoyens.findAll()).thenReturn(List.of(moi, autre, invisible));
        when(citoyens.findById(moi.getId())).thenReturn(Optional.of(moi));
        when(citoyens.findById(autre.getId())).thenReturn(Optional.of(autre));
        when(completude.estVisible(moi)).thenReturn(true);
        when(completude.estVisible(autre)).thenReturn(true);
        when(completude.estVisible(invisible)).thenReturn(false);
    }

    @Test
    void laRechercheNeRenvoiNiSoiMemeNiUnProfilIncomplet() {
        List<TalentRechercheResponse> trouves = service.rechercher(sansFiltre, null, null, moi.getId());

        assertEquals(1, trouves.size());
        assertEquals(autre.getId(), trouves.get(0).id());
    }

    @Test
    void unProfilIncompletNePeutPasRechercher() {
        Citoyen incomplet = invisible;
        when(citoyens.findById(incomplet.getId())).thenReturn(Optional.of(incomplet));

        assertThrows(IllegalStateException.class, () -> service.rechercher(sansFiltre, null, null, incomplet.getId()));
    }

    @Test
    void onNeConsultePasSonPropreProfilTalent() {
        assertThrows(ResourceNotFoundException.class, () -> service.trouverParId(moi.getId(), moi.getId()));
    }

    @Test
    void uneOrganisationVoitTousLesProfilsCompletsSansRestriction() {
        List<TalentRechercheResponse> trouves = service.rechercher(sansFiltre, null, null, null);

        assertEquals(2, trouves.size());
    }

    @Test
    void unContactEtabliNEstPlusAnonymeDansLesResultats() {
        UUID chercheur = UUID.randomUUID();
        autre.setPrenom("Aminata");
        autre.setNom("Traoré");
        when(relations.contactsEtablis(chercheur)).thenReturn(java.util.Set.of(autre.getId()));

        TalentRechercheResponse r = service.rechercher(sansFiltre, null, null, null, chercheur).stream()
                .filter(t -> t.id().equals(autre.getId())).findFirst().orElseThrow();

        assertTrue(r.enRelation());
        assertEquals("Aminata Traoré", r.nomComplet());
    }

    @Test
    void unInconnuResteAnonyme() {
        TalentRechercheResponse r = service.rechercher(sansFiltre, null, null, null, UUID.randomUUID()).get(0);

        assertFalse(r.enRelation());
        assertNull(r.nomComplet());
        assertNull(r.photoUrl());
    }

    @Test
    void leProfilCompletEstRefuseSansMiseEnRelationAcceptee() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.profilDuContact(autre.getId(), UUID.randomUUID(), false));
    }

        @Test
    void uneOrganisationEnAttenteNePeutNiChercherNiConsulterUnTalent() {
        UUID organisationId = UUID.randomUUID();
        var organisation = new odk.malicompetences.backend.organisation.Organisation();
        organisation.setStatut(odk.malicompetences.backend.organisation.StatutOrganisation.EN_ATTENTE);
        when(organisations.findById(organisationId)).thenReturn(Optional.of(organisation));

        assertThrows(IllegalStateException.class, () -> service.rechercher(sansFiltre, null, null, null, organisationId));
        assertThrows(IllegalStateException.class, () -> service.trouverParId(autre.getId(), null, organisationId));
    }

    @Test
    void uneOrganisationApprouveePeutChercher() {
        UUID organisationId = UUID.randomUUID();
        var organisation = new odk.malicompetences.backend.organisation.Organisation();
        organisation.setStatut(odk.malicompetences.backend.organisation.StatutOrganisation.APPROUVEE);
        when(organisations.findById(organisationId)).thenReturn(Optional.of(organisation));

        assertEquals(2, service.rechercher(sansFiltre, null, null, null, organisationId).size());
    }

    private static Citoyen citoyen() {
        Citoyen c = new Citoyen();
        c.setId(UUID.randomUUID());
        c.setDisponibilite(Disponibilite.DISPONIBLE);
        return c;
    }
}
