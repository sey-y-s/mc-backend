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
        String valideeParTelephone,
        UUID centreId,
        String centreNom,
        TypeValidation type,
        StatutValidation statut,
        LocalDateTime dateValidation,
        String commentaire,
        LocalDate dateExpiration,
        odk.malicompetences.backend.competence.NiveauCompetence niveauValide,
        UUID decisionCorrigeeId
) {
    public ValidationResponse(UUID id, UUID citoyenCompetenceId, UUID preuveId, UUID valideeParId,
                              String valideeParTelephone, UUID centreId, String centreNom, TypeValidation type,
                              StatutValidation statut, LocalDateTime dateValidation, String commentaire,
                              LocalDate dateExpiration) {
        this(id, citoyenCompetenceId, preuveId, valideeParId, valideeParTelephone, centreId, centreNom, type,
                statut, dateValidation, commentaire, dateExpiration, null, null);
    }
}