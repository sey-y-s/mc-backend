package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.citoyen.Disponibilite;

import java.util.List;
import java.util.UUID;

/** Profil talent anonymisé : aucun nom, contact ni photo. */
public record TalentRechercheResponse(
        UUID id,
        String pseudo,
        String metierNom,
        String secteurNom,
        String localisation,
        Disponibilite disponibilite,
        boolean estDisponible,
        List<TalentCompetenceResponse> competences,
        UUID regionId,
        String regionNom,
        UUID communeId,
        String communeNom,
        List<UUID> metierIds,
        int nbValidations,
        int nbPreuves,
                boolean aPortfolio,
        /** Vrai si une mise en relation acceptée existe avec la personne qui regarde : le profil n'est plus anonyme. */
        boolean enRelation,
        String nomComplet,
        String photoUrl
) {
}
