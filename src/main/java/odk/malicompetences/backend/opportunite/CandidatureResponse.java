package odk.malicompetences.backend.opportunite;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Candidature vue par le gestionnaire (avec le contact du candidat) ou par le citoyen lui-même.
 */
public record CandidatureResponse(
        UUID id,
        UUID opportuniteId,
        String opportuniteTitre,
        UUID citoyenId,
        String citoyenNom,
        String telephone,
        String communeNom,
        String regionNom,
        boolean locale,
        int nombreCompetences,
        int nombreCompetencesValidees,
        String message,
        StatutCandidature statut,
        LocalDateTime dateCandidature
) {}
