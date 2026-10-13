package odk.malicompetences.backend.organisation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganisationProfilRequest(
        @NotBlank(message = "Le nom de l'organisation est obligatoire")
        @Size(max = 150, message = "Le nom ne doit pas dépasser 150 caractères")
        String nom,
        String description
) {}
