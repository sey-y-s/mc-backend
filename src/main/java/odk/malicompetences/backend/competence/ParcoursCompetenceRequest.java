package odk.malicompetences.backend.competence;

import jakarta.validation.constraints.NotNull;

public record ParcoursCompetenceRequest(@NotNull ParcoursCompetence parcours) {
}
