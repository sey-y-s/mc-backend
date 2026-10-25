package odk.malicompetences.backend.common.regle;

import odk.malicompetences.backend.besoin.StatutBesoin;
import odk.malicompetences.backend.common.statut.StatutDemandeValidation;
import odk.malicompetences.backend.common.statut.StatutValidationCompetence;
import odk.malicompetences.backend.miseenrelation.StatutContact;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransitionsTest {

    @Test
    void demandeValidationFinaleNeBougePlus() {
        for (StatutDemandeValidation f : new StatutDemandeValidation[]{
                StatutDemandeValidation.VALIDEE, StatutDemandeValidation.REJETEE, StatutDemandeValidation.ANNULEE}) {
            assertTrue(Transitions.DEMANDE_VALIDATION.estFinal(f));
            assertThrows(IllegalStateException.class,
                    () -> Transitions.DEMANDE_VALIDATION.verifier(f, StatutDemandeValidation.EN_COURS));
        }
    }

    @Test
    void demandeValidationParcoursNormal() {
        Transitions.DEMANDE_VALIDATION.verifier(StatutDemandeValidation.EN_ATTENTE, StatutDemandeValidation.EN_COURS);
        Transitions.DEMANDE_VALIDATION.verifier(StatutDemandeValidation.EN_COURS, StatutDemandeValidation.COMPLEMENT_REQUIS);
        Transitions.DEMANDE_VALIDATION.verifier(StatutDemandeValidation.COMPLEMENT_REQUIS, StatutDemandeValidation.EN_COURS);
        Transitions.DEMANDE_VALIDATION.verifier(StatutDemandeValidation.EN_COURS, StatutDemandeValidation.VALIDEE);
    }

    @Test
    void resterSurLeMemeStatutEstUneErreur() {
        assertThrows(IllegalStateException.class,
                () -> Transitions.BESOIN.verifier(StatutBesoin.OUVERT, StatutBesoin.OUVERT));
    }

    @Test
    void competenceValideeNeRepassePasDirectementEnCours() {
        assertFalse(Transitions.COMPETENCE_VALIDATION.autorise(StatutValidationCompetence.VALIDEE, StatutValidationCompetence.EN_COURS));
        assertTrue(Transitions.COMPETENCE_VALIDATION.autorise(StatutValidationCompetence.VALIDEE, StatutValidationCompetence.A_REVALIDER));
        assertTrue(Transitions.COMPETENCE_VALIDATION.autorise(StatutValidationCompetence.A_REVALIDER, StatutValidationCompetence.EN_COURS));
    }

    @Test
    void besoinClotureOuAnnuleNeSeRouvrePas() {
        assertTrue(Transitions.BESOIN.estFinal(StatutBesoin.CLOTURE));
        assertTrue(Transitions.BESOIN.estFinal(StatutBesoin.ANNULE));
        assertTrue(Transitions.BESOIN.autorise(StatutBesoin.BROUILLON, StatutBesoin.OUVERT));
        assertFalse(Transitions.BESOIN.autorise(StatutBesoin.BROUILLON, StatutBesoin.CLOTURE));
    }

    @Test
    void miseEnRelationSeulEnAttenteEvolue() {
        assertTrue(Transitions.MISE_EN_RELATION.autorise(StatutContact.EN_ATTENTE, StatutContact.ACCEPTEE));
        assertFalse(Transitions.MISE_EN_RELATION.autorise(StatutContact.REFUSEE, StatutContact.ACCEPTEE));
    }

    @Test
    void toutStatutDoitEtreDeclare() {
        // Le builder refuse une table incomplete : ajouter un statut sans decider de ses transitions echoue.
        assertThrows(IllegalStateException.class, () ->
                TransitionsStatut.pour(StatutBesoin.class).de(StatutBesoin.BROUILLON, StatutBesoin.OUVERT).build());
    }
}
