package odk.malicompetences.backend.opportunite;

import jakarta.validation.constraints.NotNull;

public record CandidatureStatutRequest(@NotNull StatutCandidature statut) {
}
