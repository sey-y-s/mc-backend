package odk.malicompetences.backend.geolocalisation;

import java.util.List;
import java.util.UUID;

/**
 * Recensement des talents correspondant à une demande, regroupés par localité.
 * Données agrégées : aucune position individuelle de citoyen n'est transmise.
 */
public record CarteTalentsResponse(
        UUID besoinId,
        String besoinTitre,
        LieuDemande lieu,
        int totalTalents,
        int talentsSansLocalite,
        List<Localite> localites
) {
    /** source : BESOIN (lieu défini sur la demande), ORGANISATION (position de l'organisation) ou AUCUN. */
    public record LieuDemande(Double latitude, Double longitude, String source) {
    }

    public record Localite(
            String cle,
            UUID communeId,
            String communeNom,
            UUID regionId,
            String regionNom,
            double latitude,
            double longitude,
            boolean positionMesuree,
            int nombreTalents,
            int nombreDisponibles,
            int meilleurScore,
            int scoreMoyen,
            Double distanceKm,
            List<UUID> talentIds
    ) {
    }
}
