package odk.malicompetences.backend.citoyen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CitoyenRequest(
        @NotBlank(message = "Le nom est obligatoire.")
        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
        String nom,
        @NotBlank(message = "Le prénom est obligatoire.")
        @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères.")
        String prenom,
        @NotNull(message = "Le sexe est obligatoire.")
        Sexe sexe,
        @Size(max = 500, message = "L'URL de la photo ne peut pas dépasser 500 caractères.")
        String photoUrl,
        Disponibilite disponibilite
) {
}