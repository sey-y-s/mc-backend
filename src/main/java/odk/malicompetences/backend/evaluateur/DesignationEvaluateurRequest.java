package odk.malicompetences.backend.evaluateur;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
public record DesignationEvaluateurRequest(@NotNull UUID citoyenId,@NotNull UUID centreId,@Size(max=200) String specialite){}
