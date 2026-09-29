package odk.malicompetences.backend.competence;

import java.util.UUID;

public record CitoyenCompetenceRequest(
        UUID competenceId,
        NiveauCompetence niveau
) {
}