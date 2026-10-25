package odk.malicompetences.backend.centre;

import odk.malicompetences.backend.common.statut.StatutCentre;
import java.time.LocalDateTime;
import java.util.UUID;

public record CentreResponse(
        UUID id,
        String nom,
        String description,
        String adresse,
        String telephone,
        String email,
        UUID communeId,
        String communeNom,
        StatutCentre statut,
        LocalDateTime creeLe
) {}
