package odk.malicompetences.backend.institution;

import java.time.LocalDateTime;
import java.util.UUID;

/** Données agrégées uniquement : aucun identifiant ni effectif brut n'est exposé. */
public record InstitutionIndicateurResponse(
        String regionCode,
        String regionNom,
        UUID competenceId,
        String competenceNom,
        Integer offre,
        Integer demande,
        Integer ecart,
        String statutConfidentialite,
        String motifMasquage,
        LocalDateTime dateCalcul,
        String criteres,
        String limitesCouverture
) {}
