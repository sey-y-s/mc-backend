package odk.malicompetences.backend.testnumerique;

import java.util.UUID;

/** Question d'un test telle que renvoyée par l'API (sans le test parent ni les bonnes réponses). */
public record QuestionResponse(UUID id, String texte, TypeQuestion type, Integer ordre) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(question.getId(), question.getTexte(), question.getType(), question.getOrdre());
    }
}
