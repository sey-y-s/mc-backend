package odk.malicompetences.backend.centre;

import java.util.UUID;

public record CentreResponse(
        UUID id,
        String nom,
        String description,
        String adresse,
        String telephone,
        StatutCentre statut,
        UUID administrateurId
) {
    public CentreResponse(UUID id, String nom, String description, String adresse, String telephone) {
        this(id, nom, description, adresse, telephone, null, null);
    }
}