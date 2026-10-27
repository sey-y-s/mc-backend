package odk.malicompetences.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Demande d'un nouveau code pour un contact du compte. */
public record ResendOtpRequest(
        @NotBlank(message = "L'identifiant est obligatoire") String identifiant,
        @NotNull(message = "Le contact à vérifier est obligatoire") CanalVerification canal
) {
}
