package odk.malicompetences.backend.territoire;

import java.util.UUID;

public record CommuneResponse(
        UUID id,
        UUID regionId,
        String regionNom,
        String nom
) {}