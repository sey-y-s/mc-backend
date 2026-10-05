package odk.malicompetences.backend.miseenrelation;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConsultationProfilRequest(
        @NotNull(message = "L'ID de l'utilisateur consultant est obligatoire")
        UUID utilisateurId,

        @NotNull(message = "L'ID du citoyen consulté est obligatoire")
        UUID citoyenId
) {}