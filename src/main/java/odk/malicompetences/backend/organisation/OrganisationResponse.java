package odk.malicompetences.backend.organisation;

import java.util.UUID;

public record OrganisationResponse(
        UUID id,
        UUID utilisateurId,
        String utilisateurEmail,
        String nom,
        String description,
        StatutOrganisation statut
) {}