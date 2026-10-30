package odk.malicompetences.backend.testnumerique;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.preuve.Preuve;
import odk.malicompetences.backend.preuve.PreuveRepository;
import odk.malicompetences.backend.preuve.TypePreuve;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class TestNumeriqueService {

    private final TestNumeriqueRepository testRepository;
    private final QuestionRepository questionRepository;
    private final PropositionReponseRepository propositionRepository;
    private final ResultatTestRepository resultatRepository;
    private final CitoyenRepository citoyenRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final PreuveRepository preuveRepository;
    private final odk.malicompetences.backend.parcours.ReglesModaliteService reglesModalite;
    private final odk.malicompetences.backend.parcours.ParcoursValidationService parcoursValidation;
    private final odk.malicompetences.backend.validation.ValidationService validationService;
    private final odk.malicompetences.backend.utilisateur.UtilisateurRepository utilisateurs;
    private final odk.malicompetences.backend.notification.NotificationService notifications;

    public TestNumeriqueService(TestNumeriqueRepository testRepository,
                                QuestionRepository questionRepository,
                                PropositionReponseRepository propositionRepository,
                                ResultatTestRepository resultatRepository,
                                CitoyenRepository citoyenRepository,
                                CitoyenCompetenceRepository citoyenCompetenceRepository,
                                PreuveRepository preuveRepository,
                                odk.malicompetences.backend.parcours.ReglesModaliteService reglesModalite,
                                odk.malicompetences.backend.parcours.ParcoursValidationService parcoursValidation,
                                odk.malicompetences.backend.validation.ValidationService validationService,
                                odk.malicompetences.backend.utilisateur.UtilisateurRepository utilisateurs,
                                odk.malicompetences.backend.notification.NotificationService notifications) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.propositionRepository = propositionRepository;
        this.resultatRepository = resultatRepository;
        this.citoyenRepository = citoyenRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.preuveRepository = preuveRepository;
        this.reglesModalite = reglesModalite;
        this.parcoursValidation = parcoursValidation;
        this.validationService = validationService;
        this.utilisateurs = utilisateurs;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public TestNumerique trouver(UUID id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test numérique introuvable"));
    }

    @Transactional(readOnly = true)
    public List<TestNumerique> trouverPourCompetence(UUID competenceId) {
        return testRepository.findByCompetenceIdAndActifTrue(competenceId);
    }

    @Transactional(readOnly = true)
    public List<Question> trouverQuestions(UUID testId) {
        trouver(testId);
        return questionRepository.findByTestIdOrderByOrdreAsc(testId);
    }

    @Transactional(readOnly = true)
    public TestNumeriqueResponse trouverReponse(UUID id) {
        return TestNumeriqueResponse.from(trouver(id));
    }

    @Transactional(readOnly = true)
    public List<TestNumeriqueResponse> trouverReponsesPourCompetence(UUID competenceId) {
        return testRepository.findByCompetenceIdAndActifTrue(competenceId).stream()
                .map(TestNumeriqueResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> trouverQuestionsReponse(UUID testId) {
        return trouverQuestions(testId).stream().map(QuestionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PropositionReponse> trouverPropositions(UUID questionId) {
        return propositionRepository.findByQuestionId(questionId);
    }

    public ResultatTest soumettre(UUID testId, UUID citoyenId, Map<UUID, List<UUID>> reponses) {
        TestNumerique test = trouver(testId);
        Citoyen citoyen = citoyenRepository.findById(citoyenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citoyen introuvable"));
        CitoyenCompetence competence = citoyenCompetenceRepository
                .findByCitoyenIdAndCompetenceId(citoyenId, test.getCompetence().getId())
                .orElseThrow(() -> new IllegalStateException("Le citoyen doit d'abord déclarer cette compétence"));

        List<Question> questions = questionRepository.findByTestIdOrderByOrdreAsc(testId);
        int scored = 0;
        int correct = 0;

        for (Question question : questions) {
            List<PropositionReponse> propositions = propositionRepository.findByQuestionId(question.getId());
            if (question.getType() == TypeQuestion.TEXTE_LIBRE || propositions.isEmpty()) {
                continue;
            }
            scored++;
            Set<UUID> attendues = propositions.stream().filter(PropositionReponse::isCorrecte)
                    .map(PropositionReponse::getId).collect(java.util.stream.Collectors.toSet());
            Set<UUID> donnees = new HashSet<>(reponses.getOrDefault(question.getId(), List.of()));
            if (attendues.equals(donnees)) {
                correct++;
            }
        }

        BigDecimal score = scored == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(correct * 100.0 / scored).setScale(2, RoundingMode.HALF_UP);
        // Seuil de réussite fixé par les règles de la compétence (60 % par défaut).
        int seuil = reglesModalite.seuilReussiteTest(test.getCompetence().getId());
        boolean reussi = score.compareTo(BigDecimal.valueOf(seuil)) >= 0;

        // Seul un test réussi (60 % ou plus) devient une preuve jointe à la compétence du citoyen.
        Preuve preuve = null;
        if (reussi) {
            preuve = new Preuve();
            preuve.setCitoyenCompetence(competence);
            preuve.setType(TypePreuve.TEST_NUMERIQUE);
            preuve.setTitre("Test réussi : " + test.getTitre());
            preuve.setDescription("Score obtenu : " + score + " %");
            preuve.setDateDepot(LocalDateTime.now());
            preuve = preuveRepository.save(preuve);
            if (competence.getEtat() == odk.malicompetences.backend.competence.EtatCompetence.DECLAREE) {
                competence.setEtat(odk.malicompetences.backend.competence.EtatCompetence.ATTESTEE);
                citoyenCompetenceRepository.save(competence);
            }
        }

        ResultatTest resultat = new ResultatTest();
        resultat.setTest(test);
        resultat.setCitoyen(citoyen);
        resultat.setScore(score);
        resultat.setReussi(reussi);
        resultat.setDatePassage(LocalDateTime.now());
        resultat.setPreuve(preuve);
        ResultatTest enregistre = resultatRepository.save(resultat);
        if (reussi) {
            enregistre.setCompetenceValidee(validerParLeTest(competence, preuve, score, test));
        }
        return enregistre;
    }

    /**
     * Un test réussi valide la compétence quand c'est la démarche du parcours du citoyen (modalité « test
     * numérique » recommandée pour cette compétence). Sinon il reste une simple preuve : le parcours est imposé
     * et un test ne contourne pas la modalité prévue (ex. validation documentaire).
     * La décision est enregistrée comme les autres, au nom d'un administrateur de la plateforme.
     */
    private boolean validerParLeTest(CitoyenCompetence competence, Preuve preuve, BigDecimal score, TestNumerique test) {
        if (competence.getEtat() == odk.malicompetences.backend.competence.EtatCompetence.VALIDEE || preuve == null) {
            return false;
        }
        var parcours = parcoursValidation.proposer(competence.getId(), competence.getCitoyen().getId());
        if (parcours.recommandee() != odk.malicompetences.backend.parcours.ModaliteValidation.TEST_NUMERIQUE) {
            return false;
        }
        var decideur = utilisateurs.findAll().stream()
                .filter(odk.malicompetences.backend.utilisateur.Utilisateur::isActif)
                .filter(u -> u.getRole() == odk.malicompetences.backend.utilisateur.Role.SUPER_ADMIN
                        || u.getRole() == odk.malicompetences.backend.utilisateur.Role.ADMIN)
                .findFirst()
                .orElse(null);
        if (decideur == null) {
            return false; // la preuve reste jointe : la validation se fera à l'examen d'un administrateur
        }
        String commentaire = "Test numérique réussi : " + test.getTitre() + " (" + score + " %).";
        var demande = new odk.malicompetences.backend.validation.ValidationRequest(
                competence.getId(), preuve.getId(), null,
                odk.malicompetences.backend.validation.TypeValidation.TEST_NUMERIQUE, commentaire, null);
        var creee = validationService.creerValidation(demande, decideur.getId().toString());
        validationService.decider(creee.id(), odk.malicompetences.backend.validation.StatutValidation.APPROUVEE,
                commentaire, null, decideur.getId());
        notifications.creerEtEnvoyerNotification(new odk.malicompetences.backend.notification.NotificationRequest(
                "Compétence validée",
                "Votre test numérique est réussi : la compétence « " + competence.getCompetence().getNom() + " » est validée.",
                odk.malicompetences.backend.notification.TypeNotification.VALIDATION,
                List.of(competence.getCitoyen().getId())));
        return true;
    }
}
