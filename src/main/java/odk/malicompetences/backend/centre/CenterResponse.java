package odk.malicompetences.backend.centre;

import java.util.UUID;

public record CenterResponse(
        UUID id,
        String nom,
        String description,
        String adresse,
        String telephone
) {}