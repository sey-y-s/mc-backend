package odk.malicompetences.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Saisie du code reçu : identifiant du compte (téléphone ou email), contact vérifié et code à 6 chiffres. */
public record VerifyOtpRequest(
        @NotBlank(message = "L'identifiant est obligatoire") String identifiant,
        @NotNull(message = "Le contact à vérifier est obligatoire") CanalVerification canal,
        @NotBlank(message = "Le code est obligatoire") String code
) {
}
