package odk.malicompetences.backend.common.regle;

import odk.malicompetences.backend.besoin.StatutBesoin;
import odk.malicompetences.backend.common.statut.StatutActualisationLocalisation;
import odk.malicompetences.backend.common.statut.StatutContestation;
import odk.malicompetences.backend.common.statut.StatutDemandeValidation;
import odk.malicompetences.backend.common.statut.StatutValidationCompetence;
import odk.malicompetences.backend.miseenrelation.StatutContact;
import odk.malicompetences.backend.organisation.StatutOrganisation;
import odk.malicompetences.backend.talent.StatutSuiviBesoinTalent;

/**
 * Transitions de statuts de la V1. PROPOSITION a valider : toute modification doit s'accompagner
 * d'un test (voir TransitionsTest). Les services appellent {@code verifier(...)} avant chaque changement.
 */
public final class Transitions {

    private Transitions() {}

    /** Demande de validation. Etats finaux : VALIDEE, REJETEE, ANNULEE (une nouvelle demande est alors necessaire). */
    public static final TransitionsStatut<StatutDemandeValidation> DEMANDE_VALIDATION =
            TransitionsStatut.pour(StatutDemandeValidation.class)
                    .de(StatutDemandeValidation.EN_ATTENTE, StatutDemandeValidation.EN_COURS, StatutDemandeValidation.COMPLEMENT_REQUIS,
                            StatutDemandeValidation.VALIDEE, StatutDemandeValidation.REJETEE, StatutDemandeValidation.ANNULEE)
                    .de(StatutDemandeValidation.EN_COURS, StatutDemandeValidation.COMPLEMENT_REQUIS,
                            StatutDemandeValidation.VALIDEE, StatutDemandeValidation.REJETEE, StatutDemandeValidation.ANNULEE)
                    .de(StatutDemandeValidation.COMPLEMENT_REQUIS, StatutDemandeValidation.EN_COURS, StatutDemandeValidation.ANNULEE)
                    .termine(StatutDemandeValidation.VALIDEE, StatutDemandeValidation.REJETEE, StatutDemandeValidation.ANNULEE)
                    .build();

    /** Statut d'une competence de citoyen. VALIDEE ne repasse qu'a A_REVALIDER (expiration ou revalidation demandee). */
    public static final TransitionsStatut<StatutValidationCompetence> COMPETENCE_VALIDATION =
            TransitionsStatut.pour(StatutValidationCompetence.class)
                    .de(StatutValidationCompetence.NON_VERIFIEE, StatutValidationCompetence.EN_COURS)
                    .de(StatutValidationCompetence.EN_COURS, StatutValidationCompetence.VALIDEE, StatutValidationCompetence.REJETEE,
                            StatutValidationCompetence.NON_VERIFIEE, StatutValidationCompetence.A_REVALIDER)
                    .de(StatutValidationCompetence.VALIDEE, StatutValidationCompetence.A_REVALIDER)
                    .de(StatutValidationCompetence.REJETEE, StatutValidationCompetence.EN_COURS)
                    .de(StatutValidationCompetence.A_REVALIDER, StatutValidationCompetence.EN_COURS)
                    .build();

    /** Besoin : on ne rouvre pas un besoin cloture ou annule (en creer un nouveau). */
    public static final TransitionsStatut<StatutBesoin> BESOIN =
            TransitionsStatut.pour(StatutBesoin.class)
                    .de(StatutBesoin.BROUILLON, StatutBesoin.OUVERT, StatutBesoin.ANNULE)
                    .de(StatutBesoin.OUVERT, StatutBesoin.CLOTURE, StatutBesoin.ANNULE)
                    .termine(StatutBesoin.CLOTURE, StatutBesoin.ANNULE)
                    .build();

    /** Demande de mise en relation : seule une demande EN_ATTENTE evolue. */
    public static final TransitionsStatut<StatutContact> MISE_EN_RELATION =
            TransitionsStatut.pour(StatutContact.class)
                    .de(StatutContact.EN_ATTENTE, StatutContact.ACCEPTEE, StatutContact.REFUSEE, StatutContact.ANNULEE)
                    .termine(StatutContact.ACCEPTEE, StatutContact.REFUSEE, StatutContact.ANNULEE)
                    .build();

    /** Suivi d'un talent. Pilote par le flux de mise en relation ; l'organisation peut archiver/desarchiver. */
    public static final TransitionsStatut<StatutSuiviBesoinTalent> SUIVI =
            TransitionsStatut.pour(StatutSuiviBesoinTalent.class)
                    .de(StatutSuiviBesoinTalent.A_L_ETUDE, StatutSuiviBesoinTalent.CONTACT_EN_COURS, StatutSuiviBesoinTalent.ARCHIVE)
                    .de(StatutSuiviBesoinTalent.CONTACT_EN_COURS, StatutSuiviBesoinTalent.MISE_EN_RELATION,
                            StatutSuiviBesoinTalent.A_L_ETUDE, StatutSuiviBesoinTalent.ARCHIVE)
                    .de(StatutSuiviBesoinTalent.MISE_EN_RELATION, StatutSuiviBesoinTalent.ARCHIVE)
                    .de(StatutSuiviBesoinTalent.ARCHIVE, StatutSuiviBesoinTalent.A_L_ETUDE)
                    .build();

    /** Contestation : une contestation acceptee aboutit a une NOUVELLE decision, pas a la modification de l'ancienne. */
    public static final TransitionsStatut<StatutContestation> CONTESTATION =
            TransitionsStatut.pour(StatutContestation.class)
                    .de(StatutContestation.OUVERTE, StatutContestation.EN_EXAMEN, StatutContestation.ACCEPTEE, StatutContestation.REJETEE)
                    .de(StatutContestation.EN_EXAMEN, StatutContestation.ACCEPTEE, StatutContestation.REJETEE)
                    .termine(StatutContestation.ACCEPTEE, StatutContestation.REJETEE)
                    .build();

    public static final TransitionsStatut<StatutActualisationLocalisation> ACTUALISATION_LOCALISATION =
            TransitionsStatut.pour(StatutActualisationLocalisation.class)
                    .de(StatutActualisationLocalisation.EN_ATTENTE, StatutActualisationLocalisation.ACCEPTEE,
                            StatutActualisationLocalisation.REFUSEE, StatutActualisationLocalisation.EXPIREE)
                    .termine(StatutActualisationLocalisation.ACCEPTEE, StatutActualisationLocalisation.REFUSEE,
                            StatutActualisationLocalisation.EXPIREE)
                    .build();

    /** Verification d'organisation. Une organisation rejetee peut redeposer un dossier (retour EN_ATTENTE). */
    public static final TransitionsStatut<StatutOrganisation> ORGANISATION =
            TransitionsStatut.pour(StatutOrganisation.class)
                    .de(StatutOrganisation.EN_ATTENTE, StatutOrganisation.APPROUVEE, StatutOrganisation.REJETEE)
                    .de(StatutOrganisation.APPROUVEE, StatutOrganisation.SUSPENDUE)
                    .de(StatutOrganisation.SUSPENDUE, StatutOrganisation.APPROUVEE)
                    .de(StatutOrganisation.REJETEE, StatutOrganisation.EN_ATTENTE)
                    .build();
}
