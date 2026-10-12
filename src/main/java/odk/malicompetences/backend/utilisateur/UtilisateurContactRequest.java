package odk.malicompetences.backend.utilisateur;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UtilisateurContactRequest(
        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Size(max = 30, message = "Le téléphone ne doit pas dépasser 30 caractères")
        String telephone,

        @Email(message = "Format d'email invalide")
        @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères")
        String email
) {
}
