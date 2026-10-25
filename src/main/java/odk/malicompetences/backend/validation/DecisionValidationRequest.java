package odk.malicompetences.backend.validation;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import odk.malicompetences.backend.common.statut.DecisionValidation;
import odk.malicompetences.backend.competence.NiveauCompetence;
import java.math.BigDecimal;

public record DecisionValidationRequest(
        @NotNull DecisionValidation decision,
        NiveauCompetence niveauValide,
        @Size(max = 4000) String motif,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal score,
        @Size(max = 4000) String observations,
        JsonNode grille
) {}
