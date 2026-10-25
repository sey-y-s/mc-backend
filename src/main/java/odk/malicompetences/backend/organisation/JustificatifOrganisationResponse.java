package odk.malicompetences.backend.organisation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** La clé privée de stockage n'est jamais renvoyée à l'API cliente. */
public record JustificatifOrganisationResponse(
        UUID id, UUID organisationId, String typeJustificatif, String referenceDocument,
        LocalDate dateEmission, LocalDate dateExpiration, StatutJustificatifOrganisation statut,
        LocalDateTime verifieLe, String motifRejet
) {}
