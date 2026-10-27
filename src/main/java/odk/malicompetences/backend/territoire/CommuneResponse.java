package odk.malicompetences.backend.territoire;

import java.util.UUID;

public record CommuneResponse(
        UUID id,
        UUID regionId,
        String regionNom,
        String nom,
        UUID cercleId,
        String cercleNom
) {
    public CommuneResponse(UUID id, UUID regionId, String regionNom, String nom) {
        this(id, regionId, regionNom, nom, null, null);
    }
}