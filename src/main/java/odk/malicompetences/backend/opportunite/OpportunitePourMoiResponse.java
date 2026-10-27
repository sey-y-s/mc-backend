package odk.malicompetences.backend.opportunite;

/**
 * Opportunité vue par un citoyen connecté : proximité avec sa localité et état de sa candidature.
 * {@code proximite} : COMMUNE (sa commune), REGION (sa région), AUTRE (ailleurs), NATIONALE (tout le Mali).
 */
public record OpportunitePourMoiResponse(
        OpportuniteResponse opportunite,
        String proximite,
        boolean dejaPostule,
        StatutCandidature statutCandidature
) {}
