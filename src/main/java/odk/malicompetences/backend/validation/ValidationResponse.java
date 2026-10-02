package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.validation.StatutValidation;
import odk.malicompetences.backend.validation.TypeValidation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ValidationResponse(
        UUID id,
        UUID citoyenCompetenceId,
        UUID preuveId,
        UUID valideeParId,
        UUID centreId,
        TypeValidation type,
        StatutValidation statut,
        LocalDateTime dateValidation,
        String commentaire,
        LocalDate dateExpiration
) {
}