package odk.malicompetences.backend.evaluateur;

import odk.malicompetences.backend.common.statut.StatutHabilitation;

import java.time.LocalDate;
import java.util.UUID;

public record HabilitationEvaluateurResponse(
        UUID id,
        UUID competenceId,
        String competenceNom,
        UUID methodeId,
        String methodeCode,
        String methodeLibelle,
        UUID centreId,
        String centreNom,
        LocalDate dateDebut,
        LocalDate dateFin,
        StatutHabilitation statut
) {
    public static HabilitationEvaluateurResponse from(HabilitationEvaluateur h) {
        return new HabilitationEvaluateurResponse(
                h.getId(),
                h.getCompetence().getId(),
                h.getCompetence().getNom(),
                h.getMethode().getId(),
                h.getMethode().getCode(),
                h.getMethode().getLibelle(),
                h.getCentre() != null ? h.getCentre().getId() : null,
                h.getCentre() != null ? h.getCentre().getNom() : null,
                h.getDateDebut(),
                h.getDateFin(),
                h.getStatut()
        );
    }
}