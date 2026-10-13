package odk.malicompetences.backend.validation;

import java.time.LocalDateTime;
import java.util.UUID;

public record CitoyenValidationResponse(
        UUID id,
        TypeValidation type,
        StatutValidation statut,
        UUID citoyenCompetenceId,
        String competenceNom,
        UUID preuveId,
        String validateurLabel,
        LocalDateTime dateDemande,
        LocalDateTime dateDecision,
        String commentaire
) {
    public static CitoyenValidationResponse from(Validation validation) {
        String validateurLabel = validation.getCentre() == null
                ? (validation.getDateValidation() == null ? null : "Évaluateur")
                : validation.getCentre().getNom();
        return new CitoyenValidationResponse(
                validation.getId(),
                validation.getType(),
                validation.getStatut(),
                validation.getCitoyenCompetence().getId(),
                validation.getCitoyenCompetence().getCompetence().getNom(),
                validation.getPreuve() == null ? null : validation.getPreuve().getId(),
                validateurLabel,
                validation.getDateDemande(),
                validation.getDateValidation(),
                validation.getCommentaire()
        );
    }
}
