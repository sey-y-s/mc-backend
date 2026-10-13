package odk.malicompetences.backend.centreadmin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
public record DesignationEvaluateurCentreRequest(@NotNull UUID citoyenId, @Size(max=200) String specialite) {}
