package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.competence.NiveauCompetence;

import java.util.UUID;

/**
 * Critères de recherche de talents. Les champs texte (competence, metier, localisation) cherchent par nom ;
 * les champs UUID (competenceId, metierId, regionId, communeId) cherchent par identifiant (utilisés par le mobile).
 */
public record TalentFiltres(
        String competence,
        String metier,
        String localisation,
        String disponibilite,
        String q,
        UUID competenceId,
        UUID metierId,
        UUID regionId,
        UUID communeId,
        NiveauCompetence niveauMin
) {
}
