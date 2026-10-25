package odk.malicompetences.backend.competence;

import java.util.UUID;

public record SecteurActiviteResponse(
        UUID id,
        String code,
        String nom,
        String description,
        boolean actif
) {}
