package odk.malicompetences.backend.common.regle;

import odk.malicompetences.backend.competence.NiveauCompetence;
import odk.malicompetences.backend.common.statut.MethodeValidationCode;
import odk.malicompetences.backend.common.statut.StatutValidationCompetence;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Les colonnes de citoyen_competence modifiees par une decision de validation.
 * Valeurs nulles = pas encore valide.
 */
public record EtatValidationCompetence(
        StatutValidationCompetence statut,
        NiveauCompetence niveauValide,
        MethodeValidationCode derniereMethode,
        LocalDateTime valideeLe,
        LocalDate valideJusquAu
) {
    public static EtatValidationCompetence initial() {
        return new EtatValidationCompetence(StatutValidationCompetence.NON_VERIFIEE, null, null, null, null);
    }

    /** Vrai seulement si la competence compte dans l'OFFRE a la date donnee (VALIDEE et non expiree). */
    public boolean compteDansOffre(LocalDate aujourdhui) {
        return statut == StatutValidationCompetence.VALIDEE
                && niveauValide != null
                && valideeLe != null
                && (valideJusquAu == null || !valideJusquAu.isBefore(aujourdhui));
    }
}
