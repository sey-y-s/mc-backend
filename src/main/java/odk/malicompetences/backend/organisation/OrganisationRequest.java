package odk.malicompetences.backend.organisation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OrganisationRequest(
        @NotNull(message = "L'ID de l'utilisateur associé est obligatoire")
        UUID utilisateurId,

        @NotBlank(message = "Le nom de l'organisation est obligatoire")
        @Size(max = 150, message = "Le nom ne doit pas dépasser 150 caractères")
        String nom,

        String description,

        StatutOrganisation statut
) {}