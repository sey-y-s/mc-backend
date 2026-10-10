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

    public TestNumeriqueService(TestNumeriqueRepository testRepository,
                                QuestionRepository questionRepository,
                                PropositionReponseRepository propositionRepository,
                                ResultatTestRepository resultatRepository,
                                CitoyenRepository citoyenRepository,
                                CitoyenCompetenceRepository citoyenCompetenceRepository,
                                PreuveRepository preuveRepository) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.propositionRepository = propositionRepository;
        this.resultatRepository = resultatRepository;
        this.citoyenRepository = citoyenRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.preuveRepository = preuveRepository;
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
        boolean reussi = score.compareTo(BigDecimal.valueOf(60)) >= 0;

        Preuve preuve = new Preuve();
        preuve.setCitoyenCompetence(competence);
        preuve.setType(TypePreuve.TEST_NUMERIQUE);
        preuve.setTitre("Résultat du test : " + test.getTitre());
        preuve.setDescription("Score obtenu : " + score + " %");
        preuve.setDateDepot(LocalDateTime.now());
        preuve = preuveRepository.save(preuve);

        ResultatTest resultat = new ResultatTest();
        resultat.setTest(test);
        resultat.setCitoyen(citoyen);
        resultat.setScore(score);
        resultat.setReussi(reussi);
        resultat.setDatePassage(LocalDateTime.now());
        resultat.setPreuve(preuve);
        return resultatRepository.save(resultat);
    }
}
