package odk.malicompetences.backend.miseenrelation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultationProfilResponse(
        UUID id,
        UUID utilisateurId,
        UUID citoyenId,
        String citoyenNomComplet,
        LocalDateTime dateConsultation
) {}