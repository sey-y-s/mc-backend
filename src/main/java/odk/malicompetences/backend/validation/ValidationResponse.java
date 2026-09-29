package odk.malicompetences.backend.validation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ValidationResponse(
        UUID id,
        UUID citoyenCompetenceId,
        UUID preuveId,
        UUID valideeParId,
        String valideeParTelephone,
        UUID centreId,
        String centreNom,
        TypeValidation type,
        StatutValidation statut,
        LocalDateTime dateValidation,
        String commentaire,
        LocalDate dateExpiration
) {}