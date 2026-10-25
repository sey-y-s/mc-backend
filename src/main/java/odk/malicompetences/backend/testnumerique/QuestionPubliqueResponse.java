package odk.malicompetences.backend.testnumerique;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public record QuestionPubliqueResponse(UUID id, String enonce, TypeQuestion type, JsonNode options,
                                       BigDecimal ponderation, Integer ordre) {
    public static QuestionPubliqueResponse from(Question question) {
        return new QuestionPubliqueResponse(question.getId(), question.getTexte(), question.getType(),
                nettoyerOptions(question.getOptionsJson()), question.getPonderation(), question.getOrdre());
    }

    private static JsonNode nettoyerOptions(JsonNode options) {
        if (options == null) return null;
        JsonNode copie = options.deepCopy();
        nettoyerRecursivement(copie);
        return copie;
    }

    private static void nettoyerRecursivement(JsonNode node) {
        if (node instanceof ObjectNode object) {
            Iterator<Map.Entry<String, JsonNode>> fields = object.fields();
            java.util.List<String> aSupprimer = new java.util.ArrayList<>();
            while (fields.hasNext()) {
                String key = fields.next().getKey();
                String normalise = key.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z]", "");
                if (normalise.contains("correct") || normalise.contains("reponseattendue")
                        || normalise.contains("bonnereponse") || normalise.equals("isanswer")
                        || normalise.equals("answerkey")) {
                    aSupprimer.add(key);
                }
            }
            aSupprimer.forEach(object::remove);
            object.elements().forEachRemaining(QuestionPubliqueResponse::nettoyerRecursivement);
        } else if (node instanceof ArrayNode array) {
            array.elements().forEachRemaining(QuestionPubliqueResponse::nettoyerRecursivement);
        }
    }
}
