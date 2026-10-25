package odk.malicompetences.backend.validation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import odk.malicompetences.backend.common.statut.MethodeValidationCode;

import java.util.UUID;

public record DemandeValidationRequest(
        @NotNull MethodeValidationCode methodeCode,
        UUID centreId,
        UUID evaluateurId,
        @Size(max = 4000) String messageCitoyen
) {}
