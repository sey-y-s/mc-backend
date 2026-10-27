package odk.malicompetences.backend.preuve;

import odk.malicompetences.backend.validation.StatutValidation;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

public record CitoyenPreuveResponse(
        UUID id,
        TypePreuve type,
        UUID citoyenCompetenceId,
        String competenceNom,
        LocalDateTime date,
        String fichierNom,
        String fichierUrl,
        StatutValidation statut
) {
    public static CitoyenPreuveResponse from(Preuve preuve) {
        StatutValidation statut = preuve.getValidations().stream()
                .max(Comparator.comparing(
                        validation -> validation.getDateDemande() == null
                                ? LocalDateTime.MIN : validation.getDateDemande()
                ))
                .map(validation -> validation.getStatut())
                .orElse(null);
        String url = preuve.getFichierUrl();
        String fileName = url == null ? null : url.substring(url.lastIndexOf('/') + 1);
        return new CitoyenPreuveResponse(
                preuve.getId(),
                preuve.getType(),
                preuve.getCitoyenCompetence().getId(),
                preuve.getCitoyenCompetence().getCompetence().getNom(),
                preuve.getDateDepot(),
                fileName,
                url,
                statut
        );
    }
}
