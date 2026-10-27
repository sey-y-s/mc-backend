package odk.malicompetences.backend.institution;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** DTO strictement agrégé : aucun identifiant citoyen ni effectif brut. */
public record InstitutionIndicateurResponse(
        String regionCode,
        String regionNom,
        UUID competenceId,
        String competenceNom,
        LocalDate periodeDebut,
        LocalDate periodeFin,
        Integer offre,
        Integer demande,
        Integer ecart,
        String statutConfidentialite,
        String motifMasquage,
        LocalDateTime dateCalcul
) {}
