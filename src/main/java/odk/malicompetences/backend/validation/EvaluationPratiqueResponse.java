package odk.malicompetences.backend.validation;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EvaluationPratiqueResponse(UUID id, UUID demandeValidationId, UUID evaluateurId, UUID centreId,
                                         BigDecimal score, String observations, JsonNode grille,
                                         LocalDateTime realiseeLe) {
    public static EvaluationPratiqueResponse from(EvaluationPratique e) {
        return new EvaluationPratiqueResponse(e.getId(), e.getDemande().getId(), e.getEvaluateur().getId(),
                e.getCentre() == null ? null : e.getCentre().getId(), e.getScore(), e.getObservations(),
                e.getGrilleJson(), e.getRealiseeLe());
    }
}
