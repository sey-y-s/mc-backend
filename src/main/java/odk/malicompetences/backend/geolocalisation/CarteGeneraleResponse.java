package odk.malicompetences.backend.geolocalisation;

import java.util.List;
import java.util.UUID;

/**
 * Carte du Mali : recensement de tous les talents par localité (profils anonymisés, chiffres agrégés).
 */
public record CarteGeneraleResponse(
        int totalTalents,
        int totalDisponibles,
        int totalValides,
        int talentsSansLocalite,
        List<RegionTalents> regions,
        List<LocaliteGenerale> localites
) {

    /** Total par région, pour colorer la carte. {@code codeStable} = code de la région dans le fond de carte. */
    public record RegionTalents(UUID regionId, String regionNom, String codeStable, int nombreTalents, int nombreDisponibles) {
    }

    /** Une localité (commune, ou position partagée arrondie) et ses talents. */
    public record LocaliteGenerale(
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
            int nombreValides,
            List<String> principalesCompetences,
            List<String> principauxMetiers
    ) {
    }
}
