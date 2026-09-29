package odk.malicompetences.backend.skillsGap;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.util.UUID;

public record IndicateurSkillsGapRequest(
        @NotNull(message = "L'ID de la région est obligatoire")
        UUID regionId,

        @NotNull(message = "L'ID de la compétence est obligatoire")
        UUID competenceId,

        @NotNull(message = "La date de début de période est obligatoire")
        LocalDate periodeDebut,

        @NotNull(message = "La date de fin de période est obligatoire")
        LocalDate periodeFin,

        @NotNull(message = "La demande estimée est obligatoire")
        @PositiveOrZero(message = "La demande estimée doit être positive ou nulle")
        Integer demandeEstimee,

        @NotNull(message = "L'offre disponible est obligatoire")
        @PositiveOrZero(message = "L'offre disponible doit être positive ou nulle")
        Integer offreDisponible
) {}