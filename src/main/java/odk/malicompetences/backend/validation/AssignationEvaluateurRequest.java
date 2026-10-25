package odk.malicompetences.backend.validation;
import jakarta.validation.constraints.NotNull; import java.util.UUID;
public record AssignationEvaluateurRequest(@NotNull UUID evaluateurId) {}
