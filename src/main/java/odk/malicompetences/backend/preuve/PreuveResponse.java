package odk.malicompetences.backend.preuve;

import odk.malicompetences.backend.preuve.TypePreuve;

import java.time.LocalDateTime;
import java.util.UUID;

public record PreuveResponse(
        UUID id,
        UUID citoyenCompetenceId,
        TypePreuve type,
        String titre,
        String description,
        String fichierUrl,
        LocalDateTime dateDepot
) {
}