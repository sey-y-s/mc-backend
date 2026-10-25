package odk.malicompetences.backend.competence;

import java.util.List;
import java.util.UUID;

public record MetierResponse(
        UUID id,
        String code,
        String nom,
        String description,
        boolean actif,
        UUID secteurId,
        String secteurCode,
        String secteurNom,
        List<CompetenceResume> competences
) {
    public record CompetenceResume(UUID id, String code, String nom) {}
}
