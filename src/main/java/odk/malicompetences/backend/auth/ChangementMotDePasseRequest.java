package odk.malicompetences.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChangementMotDePasseRequest(
        @NotNull(message = "L'identifiant du compte est obligatoire")
        UUID utilisateurId,

        @NotBlank(message = "Le mot de passe actuel est obligatoire")
        String ancienMotDePasse,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
        String nouveauMotDePasse
) {
}
