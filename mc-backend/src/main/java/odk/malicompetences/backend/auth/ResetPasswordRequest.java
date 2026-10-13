package odk.malicompetences.backend.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Réinitialisation : identifiant du compte + code reçu + nouveau mot de passe. */
public record ResetPasswordRequest(
        @NotBlank(message = "L'identifiant est obligatoire")
        String identifiant,

        @NotBlank(message = "Le code est obligatoire")
        String code,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
        @JsonAlias("password")
        String motDePasse
) {
}
