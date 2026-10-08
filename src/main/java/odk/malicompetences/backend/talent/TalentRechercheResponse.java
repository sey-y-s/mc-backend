package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.citoyen.Disponibilite;

import java.util.List;
import java.util.UUID;

public record TalentRechercheResponse(
        UUID id,
        String pseudo,
        String metierNom,
        String secteurNom,
        String localisation,
        Disponibilite disponibilite,
        boolean estDisponible,
        List<TalentCompetenceResponse> competences
) {
}
