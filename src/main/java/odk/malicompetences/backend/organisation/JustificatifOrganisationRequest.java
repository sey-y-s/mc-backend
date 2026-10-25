package odk.malicompetences.backend.organisation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record JustificatifOrganisationRequest(
        @NotBlank @Size(max = 40) String typeJustificatif,
        @Size(max = 180) String referenceDocument,
        @Size(max = 500) String cleStockagePrive,
        LocalDate dateEmission,
        LocalDate dateExpiration
) {}
