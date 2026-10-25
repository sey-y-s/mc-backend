package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.common.statut.DecisionValidation;
import odk.malicompetences.backend.competence.NiveauCompetence;
import java.time.LocalDateTime;
import java.util.UUID;

public record DecisionValidationResponse(UUID id, DecisionValidation decision, NiveauCompetence niveauValide, String motif, LocalDateTime decideeLe, UUID decideurUtilisateurId) {
    public static DecisionValidationResponse from(DecisionValidationEntity e) {
        return new DecisionValidationResponse(e.getId(), e.getDecision(), e.getNiveauValide(), e.getMotif(), e.getDecideeLe(), e.getDecideur()==null?null:e.getDecideur().getId());
    }
}
