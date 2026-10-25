package odk.malicompetences.backend.talent;


import java.util.List;
import java.util.UUID;

/** Profil talent anonymisé : aucun nom, contact ni photo. */
public record TalentRechercheResponse(
        UUID id,
        String pseudo,
        String metierNom,
        String secteurNom,
        String localisation,
        boolean estDisponible,
        List<TalentCompetenceResponse> competences,
        UUID regionId,
        String regionNom,
        UUID communeId,
        String communeNom,
        List<UUID> metierIds,
        int nbValidations,
        int nbPreuves,
        boolean aPortfolio
) {
}
