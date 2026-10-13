package odk.malicompetences.backend.citoyen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CitoyenRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom,

        @Size(max = 20, message = "Le sexe ne doit pas dépasser 20 caractères")
        String sexe,

        @Size(max = 500, message = "L'URL de la photo est trop longue")
        String photoUrl,

        @NotNull(message = "La disponibilité est obligatoire")
        Disponibilite disponibilite,

        java.util.UUID communeId
) {
}