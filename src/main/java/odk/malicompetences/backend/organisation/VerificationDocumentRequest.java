package odk.malicompetences.backend.organisation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerificationDocumentRequest(
        @NotNull StatutDocumentOrganisation statut,
        @Size(max = 1000) String commentaire
) {}
