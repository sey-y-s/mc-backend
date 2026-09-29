package odk.malicompetences.backend.centre;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CentreRequest(
        @NotBlank(message = "Le nom du centre est obligatoire")
        @Size(max = 150, message = "Le nom ne doit pas dépasser 150 caractères")
        String nom,

        String description,

        @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
        String adresse,

        @Size(max = 30, message = "Le téléphone ne doit pas dépasser 30 caractères")
        String telephone
) {}