package odk.malicompetences.backend.validation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record ValidationRequest(
        @NotNull(message = "L'ID de la compétence du citoyen est obligatoire")
        UUID citoyenCompetenceId,

        UUID preuveId,

        @NotNull(message = "L'ID de l'utilisateur validateur est obligatoire")
        UUID valideeParId,

        UUID centreId,

        @NotNull(message = "Le type de validation est obligatoire")
        TypeValidation type,

        @NotNull(message = "Le statut de validation est obligatoire")
        StatutValidation statut,

        @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
        String commentaire,

        LocalDate dateExpiration
) {}