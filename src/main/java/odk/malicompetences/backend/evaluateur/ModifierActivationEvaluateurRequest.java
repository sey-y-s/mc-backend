package odk.malicompetences.backend.evaluateur;

import jakarta.validation.constraints.NotNull;

public record ModifierActivationEvaluateurRequest(@NotNull Boolean actif) {}
