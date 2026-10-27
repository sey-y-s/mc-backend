package odk.malicompetences.backend.opportunite;

import jakarta.validation.constraints.Size;

public record CandidatureRequest(@Size(max = 2000, message = "Le message ne doit pas dépasser 2000 caractères") String message) {
}
