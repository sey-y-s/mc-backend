package odk.malicompetences.backend.auth;


import jakarta.validation.constraints.NotBlank;

/** Demande d'un code de réinitialisation : téléphone ou email du compte. */
public record ForgotPasswordRequest(
        @NotBlank(message = "L'identifiant est obligatoire")
        String identifiant
) {
}
