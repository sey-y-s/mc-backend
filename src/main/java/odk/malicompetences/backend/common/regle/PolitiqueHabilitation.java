package odk.malicompetences.backend.common.regle;

import odk.malicompetences.backend.common.statut.MethodeValidationCode;
import odk.malicompetences.backend.common.statut.StatutCentre;
import odk.malicompetences.backend.common.statut.StatutEvaluateur;
import odk.malicompetences.backend.common.statut.StatutHabilitation;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Qui a le droit d'evaluer quoi. A appeler AVANT d'enregistrer une evaluation ou une decision.
 * Le droit n'est JAMAIS deduit d'un role : seule une habilitation active, valable et ciblee suffit.
 */
public final class PolitiqueHabilitation {

    private PolitiqueHabilitation() {}

    public enum Refus {
        EVALUATEUR_INACTIF,
        HABILITATION_INACTIVE,
        HABILITATION_NON_COMMENCEE,
        HABILITATION_EXPIREE,
        COMPETENCE_NON_COUVERTE,
        METHODE_NON_COUVERTE,
        CENTRE_REQUIS,
        CENTRE_NON_COUVERT,
        AUTO_EVALUATION,
        CENTRE_INACTIF,
        CENTRE_NON_AGREE
    }

    /** Habilitation telle que lue en base (evaluateur + habilitation). */
    public record HabilitationInfo(
            UUID evaluateurCitoyenId,
            StatutEvaluateur statutEvaluateur,
            StatutHabilitation statut,
            UUID competenceId,
            MethodeValidationCode methode,
            UUID centreId,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {}

    /** Ce que l'on veut evaluer. competenceId = competence de la demande (via citoyen_competence). */
    public record DemandeInfo(UUID citoyenId, UUID competenceId, MethodeValidationCode methode, UUID centreId) {}

    /** Vide = autorise. Sinon la raison du refus (a traduire en message et en code d'erreur). */
    public static Optional<Refus> verifier(HabilitationInfo h, DemandeInfo d, LocalDate aujourdhui) {
        if (h.statutEvaluateur() != StatutEvaluateur.ACTIF) return Optional.of(Refus.EVALUATEUR_INACTIF);
        if (h.statut() != StatutHabilitation.ACTIVE) return Optional.of(Refus.HABILITATION_INACTIVE);
        if (h.dateDebut() != null && aujourdhui.isBefore(h.dateDebut())) return Optional.of(Refus.HABILITATION_NON_COMMENCEE);
        if (h.dateFin() != null && aujourdhui.isAfter(h.dateFin())) return Optional.of(Refus.HABILITATION_EXPIREE);
        if (!h.competenceId().equals(d.competenceId())) return Optional.of(Refus.COMPETENCE_NON_COUVERTE);
        if (h.methode() != d.methode()) return Optional.of(Refus.METHODE_NON_COUVERTE);
        if (h.evaluateurCitoyenId().equals(d.citoyenId())) return Optional.of(Refus.AUTO_EVALUATION);
        if (d.methode() == MethodeValidationCode.EVALUATION_CENTRE) {
            if (d.centreId() == null) return Optional.of(Refus.CENTRE_REQUIS);
            if (h.centreId() == null) return Optional.of(Refus.CENTRE_NON_COUVERT);
        }
        if (h.centreId() != null && !h.centreId().equals(d.centreId())) return Optional.of(Refus.CENTRE_NON_COUVERT);
        return Optional.empty();
    }

    /** Le centre doit etre actif ET agree pour la competence a la date de l'evaluation. */
    public static Optional<Refus> verifierCentre(StatutCentre statut, boolean agreePourLaCompetence) {
        if (statut != StatutCentre.ACTIF) return Optional.of(Refus.CENTRE_INACTIF);
        if (!agreePourLaCompetence) return Optional.of(Refus.CENTRE_NON_AGREE);
        return Optional.empty();
    }
}
