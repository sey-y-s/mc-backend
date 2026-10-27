package odk.malicompetences.backend.localisation;

import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.CommuneRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Trouve la commune de référence la plus proche d'une position, dans une distance maximale.
 * Composant unique, partagé par la détection du citoyen connecté et la détection à l'inscription.
 */
@Component
public class RechercheCommuneProche {

    private final CommuneRepository communeRepository;
    private final double distanceMaxKm;

    public RechercheCommuneProche(CommuneRepository communeRepository,
                                  @Value("${app.localisation.distance-max-km:60}") double distanceMaxKm) {
        this.communeRepository = communeRepository;
        this.distanceMaxKm = distanceMaxKm;
    }

    @Transactional(readOnly = true)
    public Optional<Commune> trouver(double latitude, double longitude) {
        Commune meilleure = null;
        double meilleureDistance = Double.MAX_VALUE;
        for (Commune commune : communeRepository.findAll()) {
            if (commune.getLatitude() == null || commune.getLongitude() == null) continue;
            double d = distanceKm(latitude, longitude, commune.getLatitude(), commune.getLongitude());
            if (d < meilleureDistance) {
                meilleureDistance = d;
                meilleure = commune;
            }
        }
        return meilleureDistance <= distanceMaxKm ? Optional.ofNullable(meilleure) : Optional.empty();
    }

    static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
