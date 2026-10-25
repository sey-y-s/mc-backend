package odk.malicompetences.backend.testnumerique;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.statut.MethodeValidationCode;
import odk.malicompetences.backend.common.statut.StatutDemandeValidation;
import odk.malicompetences.backend.validation.DemandeValidation;
import odk.malicompetences.backend.validation.DemandeValidationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class TestNumeriqueService {
    private final TestNumeriqueRepository tests;
    private final QuestionRepository questions;
    private final DemandeValidationRepository demandes;
    private final TentativeTestRepository tentatives;
    private final ObjectMapper mapper;

    public TestNumeriqueService(TestNumeriqueRepository tests, QuestionRepository questions,
                                DemandeValidationRepository demandes, TentativeTestRepository tentatives,
                                ObjectMapper mapper) {
        this.tests = tests;
        this.questions = questions;
        this.demandes = demandes;
        this.tentatives = tentatives;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public TestNumerique trouver(UUID id) {
        return tests.findById(id).filter(TestNumerique::isActif)
                .orElseThrow(() -> new ResourceNotFoundException("Test numérique introuvable ou inactif"));
    }

    @Transactional(readOnly = true)
    public List<TestNumerique> trouverPourCompetence(UUID competenceId) {
        return tests.findByCompetenceIdAndActifTrue(competenceId);
    }

    @Transactional(readOnly = true)
    public int compterQuestions(UUID testId) {
        return questions.findByTestIdOrderByOrdreAsc(testId).size();
    }

    @Transactional(readOnly = true)
    public List<Question> trouverQuestions(UUID testId, UUID demandeId, String authId, boolean administrateur) {
        TestNumerique test = trouver(testId);
        if (!administrateur) {
            DemandeValidation demande = verifierDemande(test, demandeId, authId, false);
            if (demande == null) {
                throw new AccessDeniedException("Une demande de validation est obligatoire pour consulter les questions.");
            }
        }
        return questions.findByTestIdOrderByOrdreAsc(testId).stream()
                .filter(question -> question.getTest().getId().equals(testId)).toList();
    }

    public TentativeTest soumettre(UUID testId, UUID demandeId, Map<UUID, JsonNode> reponses, String authId) {
        TestNumerique test = trouver(testId);
        DemandeValidation demande = verifierDemande(test, demandeId, authId, true);

        if (tentatives.existsByDemandeIdAndTestIdAndTermineeLeIsNotNull(demandeId, testId)) {
            throw new IllegalStateException("Une tentative terminée existe déjà pour ce test et cette demande.");
        }

        List<Question> qs = questions.findByTestIdOrderByOrdreAsc(testId);
        Set<UUID> questionIds = new HashSet<>();
        for (Question question : qs) questionIds.add(question.getId());
        if (!questionIds.containsAll(reponses.keySet())) {
            throw new IllegalArgumentException("Les réponses contiennent une question qui ne fait pas partie de ce test.");
        }
        if (qs.isEmpty()) throw new IllegalStateException("Ce test ne contient aucune question.");

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal obtenu = BigDecimal.ZERO;
        for (Question question : qs) {
            JsonNode answer = reponses.get(question.getId());
            if (question.getType() == TypeQuestion.TEXTE_LIBRE) continue;
            total = total.add(question.getPonderation());
            if (answer != null && egaliteJson(question.getReponseAttendueJson(), answer)) {
                obtenu = obtenu.add(question.getPonderation());
            }
        }
        if (total.signum() == 0) {
            throw new IllegalStateException("Ce test ne contient aucune question à correction automatique.");
        }

        BigDecimal score = obtenu.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
        TentativeTest tentative = new TentativeTest();
        tentative.setDemande(demande);
        tentative.setTest(test);
        tentative.setSeuilApplique(test.getSeuilReussite());
        tentative.setScore(score);
        tentative.setReussi(score.compareTo(test.getSeuilReussite()) >= 0);
        tentative.setReponsesJson(mapper.valueToTree(reponses));
        tentative.setTermineeLe(LocalDateTime.now());
        tentatives.save(tentative);
        if (demande.getStatut() == StatutDemandeValidation.EN_ATTENTE) {
            demande.setStatut(StatutDemandeValidation.EN_COURS);
        }
        return tentative;
    }

    private DemandeValidation verifierDemande(TestNumerique test, UUID demandeId, String authId,
                                               boolean verrouiller) {
        UUID userId;
        try {
            userId = UUID.fromString(authId == null ? "" : authId);
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedException("Identifiant de compte invalide.");
        }
        if (demandeId == null) {
            throw new AccessDeniedException("Une demande de validation est obligatoire.");
        }
        DemandeValidation demande = (verrouiller ? demandes.findByIdForUpdate(demandeId) : demandes.findByIdWithRelations(demandeId))
                .orElseThrow(() -> new AccessDeniedException("Demande de validation inaccessible."));

        if (demande.getCitoyenCompetence() == null
                || demande.getCitoyenCompetence().getCitoyen() == null
                || demande.getCitoyenCompetence().getCitoyen().getUtilisateur() == null
                || !demande.getCitoyenCompetence().getCitoyen().getUtilisateur().getId().equals(userId)) {
            throw new AccessDeniedException("Cette demande ne vous appartient pas.");
        }
        if (demande.getMethode() == null
                || !MethodeValidationCode.TEST_NUMERIQUE.name().equals(demande.getMethode().getCode())) {
            throw new AccessDeniedException("La demande doit utiliser la méthode TEST_NUMERIQUE.");
        }
        if (demande.getStatut() != StatutDemandeValidation.EN_ATTENTE
                && demande.getStatut() != StatutDemandeValidation.EN_COURS) {
            throw new AccessDeniedException("Cette demande n'accepte pas de tentative de test.");
        }
        if (demande.getCitoyenCompetence().getCompetence() == null
                || !demande.getCitoyenCompetence().getCompetence().getId().equals(test.getCompetence().getId())) {
            throw new AccessDeniedException("Ce test ne correspond pas à la compétence demandée.");
        }
        return demande;
    }

    private boolean egaliteJson(JsonNode attendu, JsonNode donne) {
        if (attendu == null || donne == null) return false;
        if (attendu.isArray() && donne.isArray()) {
            List<String> a = new ArrayList<>();
            List<String> b = new ArrayList<>();
            attendu.forEach(node -> a.add(node.toString()));
            donne.forEach(node -> b.add(node.toString()));
            Collections.sort(a);
            Collections.sort(b);
            return a.equals(b);
        }
        return attendu.equals(donne);
    }
}
