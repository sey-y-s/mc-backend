package odk.malicompetences.backend.besoin;

import java.util.UUID;

/** Une seule portée par ligne : commune ou territoire. Une liste vide signifie portée nationale. */
public record BesoinZoneRequest(UUID communeId, UUID territoireId) {
    public boolean valide() { return (communeId != null) != (territoireId != null); }
}
