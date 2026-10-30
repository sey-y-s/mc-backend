package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.citoyen.Disponibilite;
import odk.malicompetences.backend.portfolio.PortfolioRealisationResponse;

import java.util.List;
import java.util.UUID;

/** Profil complet d'une personne avec qui une mise en relation a été acceptée : identité, photo, compétences, réalisations. */
public record ProfilContactResponse(
        UUID id,
        String prenom,
        String nom,
        String photoUrl,
        String communeNom,
        String regionNom,
        Disponibilite disponibilite,
        List<TalentCompetenceResponse> competences,
        List<PortfolioRealisationResponse> realisations
) {
}
