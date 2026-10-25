package odk.malicompetences.backend.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComplementValidationRequest(@NotBlank @Size(max = 4000) String message) {}
