package odk.malicompetences.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import odk.malicompetences.backend.common.statut.UsageCodeVerification;

public record ResendOtpRequest(
        @NotBlank(message = "Le destinataire est obligatoire")
        String destinataire,

        @NotNull(message = "L'usage est obligatoire")
        UsageCodeVerification usage
) {}

