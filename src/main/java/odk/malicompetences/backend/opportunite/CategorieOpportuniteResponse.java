package odk.malicompetences.backend.opportunite;


import java.util.UUID;

public record CategorieOpportuniteResponse(
        UUID id,
        String nom,
        String description
) {}