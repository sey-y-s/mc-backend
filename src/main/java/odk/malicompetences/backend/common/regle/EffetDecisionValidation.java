package odk.malicompetences.backend.common.regle;

import odk.malicompetences.backend.competence.NiveauCompetence;
import odk.malicompetences.backend.common.statut.DecisionValidation;
import odk.malicompetences.backend.common.statut.MethodeValidationCode;
import odk.malicompetences.backend.common.statut.StatutDemandeValidation;
import odk.malicompetences.backend.common.statut.StatutValidationCompetence;

import java.time.LocalDateTime;

/**
 * Calcule, SANS toucher a la base, l'effet d'une decision sur la competence du citoyen.
 *
 * <p>Le service de validation doit, dans UNE SEULE transaction :
 * (1) inserer la decision (jamais de modification ni de suppression), (2) appliquer
 * {@link #appliquer} a citoyen_competence, (3) passer la demande a {@link #statutDemandeApres},
 * (4) creer la notification. Regles proposees (a valider) :
 * <ul>
 *   <li>VALIDEE : niveau valide et methode obligatoires ; date de validation = maintenant ; fin de validite
 *       = maintenant + validiteMois si fourni.</li>
 *   <li>REJETEE : statut REJETEE ; les champs de la validation precedente sont conserves (historique).</li>
 *   <li>COMPLEMENT_REQUIS : la competence reste EN_COURS.</li>
 *   <li>ANNULEE : retour a A_REVALIDER si la competence avait deja ete validee, sinon NON_VERIFIEE.</li>
 * </ul>
 */
public final class EffetDecisionValidation {

    private EffetDecisionValidation() {}

    public static EtatValidationCompetence appliquer(
            EtatValidationCompetence actuel,
            DecisionValidation decision,
            NiveauCompetence niveauDecide,
            MethodeValidationCode methode,
            LocalDateTime maintenant,
            Integer validiteMois
    ) {
        if (actuel == null || decision == null || maintenant == null) {
            throw new IllegalArgumentException("etat actuel, decision et date sont obligatoires");
        }
        if (validiteMois != null && validiteMois <= 0) {
            throw new IllegalArgumentException("validiteMois doit etre positif");
        }
        EtatValidationCompetence resultat = switch (decision) {
            case VALIDEE -> {
                if (niveauDecide == null) {
                    throw new IllegalArgumentException("Une decision VALIDEE exige le niveau valide");
                }
                if (methode == null) {
                    throw new IllegalArgumentException("Une decision VALIDEE exige la methode utilisee");
                }
                yield new EtatValidationCompetence(
                        StatutValidationCompetence.VALIDEE, niveauDecide, methode, maintenant,
                        validiteMois == null ? null : maintenant.toLocalDate().plusMonths(validiteMois));
            }
            case REJETEE -> avecStatut(actuel, StatutValidationCompetence.REJETEE);
            case COMPLEMENT_REQUIS -> avecStatut(actuel, StatutValidationCompetence.EN_COURS);
            case ANNULEE -> avecStatut(actuel, actuel.valideeLe() == null
                    ? StatutValidationCompetence.NON_VERIFIEE
                    : StatutValidationCompetence.A_REVALIDER);
        };
        if (resultat.statut() != actuel.statut()) {
            Transitions.COMPETENCE_VALIDATION.verifier(actuel.statut(), resultat.statut());
        }
        return resultat;
    }

    /** Statut de la demande apres la decision. */
    public static StatutDemandeValidation statutDemandeApres(DecisionValidation decision) {
        return switch (decision) {
            case VALIDEE -> StatutDemandeValidation.VALIDEE;
            case REJETEE -> StatutDemandeValidation.REJETEE;
            case COMPLEMENT_REQUIS -> StatutDemandeValidation.COMPLEMENT_REQUIS;
            case ANNULEE -> StatutDemandeValidation.ANNULEE;
        };
    }

    private static EtatValidationCompetence avecStatut(EtatValidationCompetence e, StatutValidationCompetence statut) {
        return new EtatValidationCompetence(statut, e.niveauValide(), e.derniereMethode(), e.valideeLe(), e.valideJusquAu());
    }
}
