package odk.malicompetences.backend.opportunite;
import jakarta.validation.constraints.*; import java.time.LocalDate; import java.util.UUID;
public record OpportuniteRequest(@NotNull UUID categorieId,@NotBlank @Size(max=200) String titre,@NotBlank String description,@NotBlank @Size(max=180) String organisme,@Size(max=500) String lienExterne,@Size(max=190) String contact,UUID territoireId,@NotNull StatutOpportunite statut,LocalDate dateExpiration) {}
