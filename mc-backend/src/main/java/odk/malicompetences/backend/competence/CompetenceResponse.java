package odk.malicompetences.backend.competence;

import java.util.UUID;

public record CompetenceResponse(
        UUID id,
        String nom,
        String description
) {
}