package odk.malicompetences.backend.validation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ValidationStatutRequest(
        @NotNull(message = "Le statut est obligatoire")
        StatutValidation statut,

        @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
        String commentaire
) {}
