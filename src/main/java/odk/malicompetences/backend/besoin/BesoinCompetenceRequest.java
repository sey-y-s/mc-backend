package odk.malicompetences.backend.besoin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import odk.malicompetences.backend.competence.NiveauCompetence;

import java.util.UUID;

public record BesoinCompetenceRequest(
        @NotNull(message = "L'ID de la compétence est obligatoire")
        UUID competenceId,

        @NotNull(message = "Le niveau minimum est obligatoire")
        NiveauCompetence niveauMinimum,

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité minimale est de 1")
        Integer quantite
) {}