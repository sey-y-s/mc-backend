package odk.malicompetences.backend.parcours;

import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.centre.HabilitationService;
import odk.malicompetences.backend.centre.StatutCentre;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.evaluateur.EvaluateurCentre;
import odk.malicompetences.backend.evaluateur.EvaluateurCentreRepository;
import odk.malicompetences.backend.portfolio.PortfolioRealisation;
import odk.malicompetences.backend.portfolio.PortfolioRealisationRepository;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.Region;
import odk.malicompetences.backend.testnumerique.TestNumeriqueRepository;
import odk.malicompetences.backend.validation.ValidationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParcoursValidationServiceTest {

    private final CitoyenCompetenceRepository citoyenCompetences = mock(CitoyenCompetenceRepository.class);
    private final ReglesModaliteService regles = mock(ReglesModaliteService.class);
    private final CentreRepository centres = mock(CentreRepository.class);
    private final HabilitationService habilitation = mock(HabilitationService.class);
    private final EvaluateurCentreRepository evaluateurs = mock(EvaluateurCentreRepository.class);
    private final TestNumeriqueRepository tests = mock(TestNumeriqueRepository.class);
    private final PortfolioRealisationRepository portfolios = mock(PortfolioRealisationRepository.class);
    private final ValidationRepository validations = mock(ValidationRepository.class);

    private final ParcoursValidationService service = new ParcoursValidationService(
            citoyenCompetences, regles, centres, habilitation, evaluateurs, tests, portfolios, validations);

    private final UUID citoyenId = UUID.randomUUID();
    private final Region regionCitoyen = region();
    private final Commune communeCitoyen = commune(regionCitoyen);
    private CitoyenCompetence competenceDuCitoyen;

    @BeforeEach
    void preparer() {
        Citoyen citoyen = new Citoyen();
        citoyen.setId(citoyenId);
        citoyen.setCommune(communeCitoyen);

        Competence competence = new Competence();
        competence.setId(UUID.randomUUID());
        competence.setNom("Maçonnerie");

        competenceDuCitoyen = new CitoyenCompetence();
        competenceDuCitoyen.setId(UUID.randomUUID());
        competenceDuCitoyen.setCitoyen(citoyen);
        competenceDuCitoyen.setCompetence(competence);
        competenceDuCitoyen.setEtat(EtatCompetence.DECLAREE);

        when(citoyenCompetences.findById(competenceDuCitoyen.getId())).thenReturn(java.util.Optional.of(competenceDuCitoyen));
    }

    private static Region region() {
        Region region = new Region();
        region.setId(UUID.randomUUID());
        return region;
    }

    private static Commune commune(Region region) {
        Commune commune = new Commune();
        commune.setId(UUID.randomUUID());
        commune.setRegion(region);
        return commune;
    }

    private static RegleModalite regle(ModaliteValidation modalite, int ordre) {
        return new RegleModalite(modalite, modalite.libelle(), ordre, true, null, List.of(), null, null, false);
    }

    private void reglesDeLaCompetence(RegleModalite... liste) {
        when(regles.regles(competenceDuCitoyen.getCompetence(), null)).thenReturn(List.of(liste));
    }

    /** Un centre actif et agréé, avec un évaluateur habilité, dans la commune donnée. */
    private void unCentre(Commune commune, String specialite) {
        Centre centre = new Centre();
        centre.setId(UUID.randomUUID());
        centre.setNom("Centre test");
        centre.setStatut(StatutCentre.ACTIF);
        centre.setCommune(commune);

        Citoyen autre = new Citoyen();
        autre.setId(UUID.randomUUID());
        EvaluateurCentre evaluateur = new EvaluateurCentre();
        evaluateur.setCitoyen(autre);
        evaluateur.setCentre(centre);
        evaluateur.setSpecialite(specialite);

        when(centres.findAll()).thenReturn(List.of(centre));
        when(habilitation.centreAgree(centre, competenceDuCitoyen.getCompetence())).thenReturn(true);
        when(evaluateurs.findByCentreIdAndActifTrue(centre.getId())).thenReturn(List.of(evaluateur));
        when(habilitation.evaluateurHabilite(evaluateur, competenceDuCitoyen.getCompetence())).thenReturn(true);
    }

    @Test
    void recommandeLaPremiereModaliteDisponible() {
        reglesDeLaCompetence(regle(ModaliteValidation.DOCUMENTAIRE, 0), regle(ModaliteValidation.TEST_NUMERIQUE, 1));

        ParcoursValidationResponse reponse = service.proposer(competenceDuCitoyen.getId(), citoyenId);

        assertEquals(ModaliteValidation.DOCUMENTAIRE, reponse.recommandee());
        assertEquals(StatutParcours.DECLAREE, reponse.statut());
        assertFalse(reponse.enAttenteEvaluation());
    }

    @Test
    void proposeUneAlternativeQuandLeCentreNExistePas() {
        reglesDeLaCompetence(regle(ModaliteValidation.PRATIQUE_CENTRE, 0), regle(ModaliteValidation.DOCUMENTAIRE, 1));

        ParcoursValidationResponse reponse = service.proposer(competenceDuCitoyen.getId(), citoyenId);

        assertEquals(ModaliteValidation.DOCUMENTAIRE, reponse.recommandee());
        assertFalse(reponse.modalites().get(0).disponible());
        assertTrue(reponse.message().contains("Alternative proposée"));
    }

    @Test
    void sansAucuneModaliteRealisableLeDossierResteEnAttente() {
        reglesDeLaCompetence(regle(ModaliteValidation.PRATIQUE_CENTRE, 0));

        ParcoursValidationResponse reponse = service.proposer(competenceDuCitoyen.getId(), citoyenId);

        assertTrue(reponse.enAttenteEvaluation());
        assertNull(reponse.recommandee());
        assertEquals(StatutParcours.DECLAREE, reponse.statut());
    }

    @Test
    void unCentreDansLaRegionPermetLEvaluationPratique() {
        unCentre(commune(regionCitoyen), null);
        reglesDeLaCompetence(regle(ModaliteValidation.PRATIQUE_CENTRE, 0));

        ParcoursValidationResponse reponse = service.proposer(competenceDuCitoyen.getId(), citoyenId);

        assertEquals(ModaliteValidation.PRATIQUE_CENTRE, reponse.recommandee());
        assertEquals(Proximite.MEME_REGION, reponse.modalites().get(0).centres().get(0).proximite());
    }

    @Test
    void unCentreD_uneAutreRegionNeSertQuALEvaluationADistance() {
        unCentre(commune(region()), null);
        when(portfolios.findByCitoyenId(citoyenId)).thenReturn(List.of(new PortfolioRealisation()));
        reglesDeLaCompetence(regle(ModaliteValidation.PRATIQUE_CENTRE, 0), regle(ModaliteValidation.PORTFOLIO_MEDIA, 1));

        ParcoursValidationResponse reponse = service.proposer(competenceDuCitoyen.getId(), citoyenId);

        assertFalse(reponse.modalites().get(0).disponible());
        assertEquals(ModaliteValidation.PORTFOLIO_MEDIA, reponse.recommandee());
        assertEquals(Proximite.AUTRE_REGION, reponse.modalites().get(1).centres().get(0).proximite());
    }

    @Test
    void leMaitreArtisanExigeUnEvaluateurAvecUneSpecialite() {
        unCentre(communeCitoyen, null);
        reglesDeLaCompetence(regle(ModaliteValidation.MAITRE_ARTISAN, 0));
        assertTrue(service.proposer(competenceDuCitoyen.getId(), citoyenId).enAttenteEvaluation());

        unCentre(communeCitoyen, "Maçonnerie");
        ParcoursValidationResponse reponse = service.proposer(competenceDuCitoyen.getId(), citoyenId);
        assertEquals(ModaliteValidation.MAITRE_ARTISAN, reponse.recommandee());
        assertEquals(Proximite.MEME_COMMUNE, reponse.modalites().get(0).centres().get(0).proximite());
    }

    @Test
    void uneCompetenceValideeNeRecoitAucuneRecommandation() {
        competenceDuCitoyen.setEtat(EtatCompetence.VALIDEE);
        reglesDeLaCompetence(regle(ModaliteValidation.DOCUMENTAIRE, 0));

        ParcoursValidationResponse reponse = service.proposer(competenceDuCitoyen.getId(), citoyenId);

        assertEquals(StatutParcours.VALIDEE, reponse.statut());
        assertNull(reponse.recommandee());
        assertFalse(reponse.enAttenteEvaluation());
    }

    @Test
    void unCitoyenNeVoitPasLeParcoursDeLAutre() {
        reglesDeLaCompetence(regle(ModaliteValidation.DOCUMENTAIRE, 0));

        assertThrows(AccessDeniedException.class,
                () -> service.proposer(competenceDuCitoyen.getId(), UUID.randomUUID()));
    }
}
