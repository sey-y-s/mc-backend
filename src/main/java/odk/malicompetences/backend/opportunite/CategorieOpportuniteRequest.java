package odk.malicompetences.backend.opportunite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CategorieOpportuniteRequest(
        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        @Size(max = 150, message = "Le nom ne doit pas dépasser 150 caractères")
        String nom,

        String description
) {}