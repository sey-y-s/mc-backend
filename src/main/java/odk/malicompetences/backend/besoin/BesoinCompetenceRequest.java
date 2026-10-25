package odk.malicompetences.backend.besoin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import odk.malicompetences.backend.competence.NiveauCompetence;

import java.util.UUID;

public record BesoinCompetenceRequest(
        @NotNull(message = "L'ID de la compétence est obligatoire")
        UUID competenceId,

        NiveauCompetence niveauRequis,

        @NotNull(message = "Le nombre requis est obligatoire")
        @Min(value = 1, message = "Le nombre requis doit être au moins égal à 1")
        Integer nombreRequis
) {}
