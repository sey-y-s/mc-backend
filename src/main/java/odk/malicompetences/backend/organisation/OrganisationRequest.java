package odk.malicompetences.backend.organisation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganisationRequest(
        @NotBlank(message = "Le nom de l'organisation est obligatoire")
        @Size(max = 150, message = "Le nom ne doit pas dépasser 150 caractères")
        String nom,
        String description,
        @Size(max = 80, message = "Le numéro RCCM ne doit pas dépasser 80 caractères")
        String numeroRccm,
        StatutOrganisation statut
) {}
