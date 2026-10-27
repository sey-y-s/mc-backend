package odk.malicompetences.backend.parcours;

import odk.malicompetences.backend.competence.ParcoursCompetence;
import odk.malicompetences.backend.preuve.TypePreuve;

import java.util.List;
import java.util.UUID;

/**
 * Parcours de validation proposé à un citoyen pour l'une de ses compétences.
 *
 * @param recommandee           modalité conseillée (null si la compétence est validée, en cours d'évaluation ou si rien n'est disponible)
 * @param enAttenteEvaluation   vrai quand aucune modalité n'est réalisable sur le territoire : le dossier attend, il n'est pas rejeté
 */
public record ParcoursValidationResponse(UUID citoyenCompetenceId,
                                         UUID competenceId,
                                         String competenceNom,
                                         ParcoursCompetence parcours,
                                         StatutParcours statut,
                                         String statutLibelle,
                                         boolean enAttenteEvaluation,
                                         ModaliteValidation recommandee,
                                         String message,
                                         List<ModaliteProposee> modalites) {

    /**
     * @param action codes : DEPOSER_PREUVE, PASSER_TEST, DEMANDER_EVALUATION, AJOUTER_REALISATIONS (null si indisponible)
     */
    public record ModaliteProposee(ModaliteValidation modalite,
                                   String libelle,
                                   String description,
                                   boolean disponible,
                                   boolean recommandee,
                                   String raison,
                                   String action,
                                   UUID testId,
                                   Integer seuilReussite,
                                   List<TypePreuve> piecesAdmissibles,
                                   String criteres,
                                   String profilsHabilites,
                                   List<CentreProche> centres) {
    }

    public record CentreProche(UUID id, String nom, String adresse, Proximite proximite, String proximiteLibelle) {
    }
}
