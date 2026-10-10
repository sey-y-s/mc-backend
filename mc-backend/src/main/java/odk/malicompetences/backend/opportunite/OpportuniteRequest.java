package odk.malicompetences.backend.opportunite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record OpportuniteRequest(
        @NotNull(message = "L'ID de la catégorie est obligatoire")
        UUID categorieId,

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
        String titre,

        @NotBlank(message = "La description est obligatoire")
        String description,

        @NotNull(message = "Le statut de l'opportunité est obligatoire")
        StatutOpportunite statut,

        @NotNull(message = "Le type d'opportunité est obligatoire")
        TypeOpportunite type,

        LocalDate dateExpiration
) {}