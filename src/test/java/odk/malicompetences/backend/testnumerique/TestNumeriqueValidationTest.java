package odk.malicompetences.backend.testnumerique;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.notification.NotificationService;
import odk.malicompetences.backend.parcours.ModaliteValidation;
import odk.malicompetences.backend.parcours.ParcoursValidationResponse;
import odk.malicompetences.backend.parcours.ParcoursValidationService;
import odk.malicompetences.backend.parcours.ReglesModaliteService;
import odk.malicompetences.backend.preuve.Preuve;
import odk.malicompetences.backend.preuve.PreuveRepository;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.validation.StatutValidation;
import odk.malicompetences.backend.validation.TypeValidation;
import odk.malicompetences.backend.validation.ValidationRequest;
import odk.malicompetences.backend.validation.ValidationResponse;
import odk.malicompetences.backend.validation.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Réussir un test valide la compétence uniquement quand le test est la démarche prévue par son parcours. */
class TestNumeriqueValidationTest {

    private final TestNumeriqueRepository tests = mock(TestNumeriqueRepository.class);
    private final QuestionRepository questions = mock(QuestionRepository.class);
    private final PropositionReponseRepository propositions = mock(PropositionReponseRepository.class);
    private final ResultatTestRepository resultats = mock(ResultatTestRepository.class);
    private final CitoyenRepository citoyens = mock(CitoyenRepository.class);
    private final CitoyenCompetenceRepository competences = mock(CitoyenCompetenceRepository.class);
    private final PreuveRepository preuves = mock(PreuveRepository.class);
    private final ReglesModaliteService regles = mock(ReglesModaliteService.class);
    private final ParcoursValidationService parcours = mock(ParcoursValidationService.class);
    private final ValidationService validations = mock(ValidationService.class);
    private final UtilisateurRepository utilisateurs = mock(UtilisateurRepository.class);
    private final NotificationService notifications = mock(NotificationService.class);

    private final TestNumeriqueService service = new TestNumeriqueService(tests, questions, propositions, resultats,
            citoyens, competences, preuves, regles, parcours, validations, utilisateurs, notifications);

    private final UUID citoyenId = UUID.randomUUID();
    private final UUID testId = UUID.randomUUID();
    private final UUID questionId = UUID.randomUUID();
    private final UUID bonneReponse = UUID.randomUUID();
    private final CitoyenCompetence competence = new CitoyenCompetence();
    private final Utilisateur admin = new Utilisateur();

    @BeforeEach
    void preparer() {
        Competence reference = new Competence();
        reference.setId(UUID.randomUUID());
        reference.setNom("Maçonnerie");
        TestNumerique test = new TestNumerique();
        test.setId(testId);
        test.setCompetence(reference);
        test.setTitre("Lecture de plans");
        Citoyen citoyen = new Citoyen();
        citoyen.setId(citoyenId);
        competence.setId(UUID.randomUUID());
        competence.setCitoyen(citoyen);
        competence.setCompetence(reference);
        competence.setEtat(EtatCompetence.DECLAREE);
        admin.setId(UUID.randomUUID());
        admin.setRole(Role.ADMIN);
        admin.setActif(true);

        when(tests.findById(testId)).thenReturn(Optional.of(test));
        when(citoyens.findById(citoyenId)).thenReturn(Optional.of(citoyen));
        when(competences.findByCitoyenIdAndCompetenceId(citoyenId, reference.getId())).thenReturn(Optional.of(competence));
        Question question = new Question();
        question.setId(questionId);
        question.setType(TypeQuestion.CHOIX_MULTIPLE);
        PropositionReponse juste = new PropositionReponse();
        juste.setId(bonneReponse);
        juste.setCorrecte(true);
        PropositionReponse fausse = new PropositionReponse();
        fausse.setId(UUID.randomUUID());
        fausse.setCorrecte(false);
        when(questions.findByTestIdOrderByOrdreAsc(testId)).thenReturn(List.of(question));
        when(propositions.findByQuestionId(questionId)).thenReturn(List.of(juste, fausse));
        when(regles.seuilReussiteTest(any())).thenReturn(60);
        when(preuves.save(any(Preuve.class))).thenAnswer(i -> {
            Preuve p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(resultats.save(any(ResultatTest.class))).thenAnswer(i -> i.getArgument(0));
        when(utilisateurs.findAll()).thenReturn(List.of(admin));
        when(validations.creerValidation(any(ValidationRequest.class), any()))
                .thenReturn(new ValidationResponse(UUID.randomUUID(), competence.getId(), null, null, null, null, null,
                        TypeValidation.TEST_NUMERIQUE, StatutValidation.EN_ATTENTE, null, null, null));
    }

    private void parcoursRecommande(ModaliteValidation modalite) {
        ParcoursValidationResponse reponse = new ParcoursValidationResponse(competence.getId(), null, "Maçonnerie", null,
                null, "", false, modalite, "", List.of());
        when(parcours.proposer(eq(competence.getId()), eq(citoyenId))).thenReturn(reponse);
    }

    @Test
    void testReussiValideLaCompetenceQuandLeTestEstLaDemarcheDuParcours() {
        parcoursRecommande(ModaliteValidation.TEST_NUMERIQUE);

        ResultatTest resultat = service.soumettre(testId, citoyenId, Map.of(questionId, List.of(bonneReponse)));

        assertTrue(resultat.getReussi());
        assertTrue(resultat.isCompetenceValidee());
        ArgumentCaptor<ValidationRequest> demande = ArgumentCaptor.forClass(ValidationRequest.class);
        verify(validations).creerValidation(demande.capture(), eq(admin.getId().toString()));
        assertEquals(TypeValidation.TEST_NUMERIQUE, demande.getValue().type());
        assertEquals(null, demande.getValue().centreId());
        verify(validations).decider(any(), eq(StatutValidation.APPROUVEE), any(), eq(null), eq(admin.getId()));
        verify(notifications).creerEtEnvoyerNotification(any());
    }

    @Test
    void testReussiResteUneSimplePreuveQuandLeParcoursImposeUneAutreDemarche() {
        parcoursRecommande(ModaliteValidation.DOCUMENTAIRE);

        ResultatTest resultat = service.soumettre(testId, citoyenId, Map.of(questionId, List.of(bonneReponse)));

        assertTrue(resultat.getReussi());
        assertFalse(resultat.isCompetenceValidee());
        verify(validations, never()).creerValidation(any(), any());
        verify(validations, never()).decider(any(), any(), any(), any(), any());
    }

    @Test
    void testRateNeValidePasEtNAjouteAucunePreuve() {
        ResultatTest resultat = service.soumettre(testId, citoyenId, Map.of(questionId, List.of(UUID.randomUUID())));

        assertFalse(resultat.getReussi());
        assertFalse(resultat.isCompetenceValidee());
        verify(preuves, never()).save(any(Preuve.class));
        verify(validations, never()).creerValidation(any(), any());
    }
}
