package odk.malicompetences.backend.competence;

import java.util.List;
import java.util.UUID;

public record CompetenceResponse(
        UUID id,
        String code,
        String nom,
        String description,
        boolean actif,
        List<MetierResume> metiers
) {
    public record MetierResume(UUID id, String code, String nom, UUID secteurId, String secteurNom) {}
}
