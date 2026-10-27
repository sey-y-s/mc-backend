package odk.malicompetences.backend.validation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ValidationStatutRequest(
        @NotNull(message = "Le statut est obligatoire")
        StatutValidation statut,

        @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
        String commentaire,

        /** Niveau reconnu (facultatif) : par défaut, le niveau déclaré par le citoyen. */
        odk.malicompetences.backend.competence.NiveauCompetence niveauValide
) {
    public ValidationStatutRequest(StatutValidation statut, String commentaire) {
        this(statut, commentaire, null);
    }
}
