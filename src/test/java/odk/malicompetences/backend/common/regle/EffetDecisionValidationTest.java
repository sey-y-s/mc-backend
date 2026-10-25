package odk.malicompetences.backend.common.regle;

import odk.malicompetences.backend.common.statut.DecisionValidation;
import odk.malicompetences.backend.common.statut.MethodeValidationCode;
import odk.malicompetences.backend.common.statut.StatutDemandeValidation;
import odk.malicompetences.backend.common.statut.StatutValidationCompetence;
import odk.malicompetences.backend.competence.NiveauCompetence;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EffetDecisionValidationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 22, 10, 0);

    private static EtatValidationCompetence enCours() {
        return new EtatValidationCompetence(StatutValidationCompetence.EN_COURS, null, null, null, null);
    }

    @Test
    void validationRenseigneNiveauMethodeEtDates() {
        EtatValidationCompetence e = EffetDecisionValidation.appliquer(
                enCours(), DecisionValidation.VALIDEE, NiveauCompetence.INTERMEDIAIRE,
                MethodeValidationCode.MAITRE_ARTISAN, NOW, 24);
        assertEquals(StatutValidationCompetence.VALIDEE, e.statut());
        assertEquals(NiveauCompetence.INTERMEDIAIRE, e.niveauValide());
        assertEquals(MethodeValidationCode.MAITRE_ARTISAN, e.derniereMethode());
        assertEquals(NOW, e.valideeLe());
        assertEquals(LocalDate.of(2028, 10, 22), e.valideJusquAu());
        assertTrue(e.compteDansOffre(LocalDate.of(2027, 1, 1)));
        assertFalse(e.compteDansOffre(LocalDate.of(2029, 1, 1)));
    }

    @Test
    void validationSansNiveauOuSansMethodeEstRefusee() {
        assertThrows(IllegalArgumentException.class, () -> EffetDecisionValidation.appliquer(
                enCours(), DecisionValidation.VALIDEE, null, MethodeValidationCode.DOCUMENT, NOW, null));
        assertThrows(IllegalArgumentException.class, () -> EffetDecisionValidation.appliquer(
                enCours(), DecisionValidation.VALIDEE, NiveauCompetence.EXPERT, null, NOW, null));
    }

    @Test
    void rejetNeCompteJamaisDansOffre() {
        EtatValidationCompetence e = EffetDecisionValidation.appliquer(
                enCours(), DecisionValidation.REJETEE, null, MethodeValidationCode.DOCUMENT, NOW, null);
        assertEquals(StatutValidationCompetence.REJETEE, e.statut());
        assertFalse(e.compteDansOffre(NOW.toLocalDate()));
    }

    @Test
    void complementRequisLaisseLaCompetenceEnCours() {
        EtatValidationCompetence e = EffetDecisionValidation.appliquer(
                enCours(), DecisionValidation.COMPLEMENT_REQUIS, null, MethodeValidationCode.PORTFOLIO, NOW, null);
        assertEquals(StatutValidationCompetence.EN_COURS, e.statut());
    }

    @Test
    void annulationRevientAuStatutAdapte() {
        assertEquals(StatutValidationCompetence.NON_VERIFIEE, EffetDecisionValidation.appliquer(
                enCours(), DecisionValidation.ANNULEE, null, MethodeValidationCode.DOCUMENT, NOW, null).statut());
        EtatValidationCompetence dejaValidee = new EtatValidationCompetence(StatutValidationCompetence.EN_COURS,
                NiveauCompetence.EXPERT, MethodeValidationCode.DOCUMENT, NOW.minusYears(3), null);
        assertEquals(StatutValidationCompetence.A_REVALIDER, EffetDecisionValidation.appliquer(
                dejaValidee, DecisionValidation.ANNULEE, null, MethodeValidationCode.DOCUMENT, NOW, null).statut());
    }

    @Test
    void competenceDejaValideeNePeutPasEtreRevalideeSansPasserParARevalider() {
        EtatValidationCompetence validee = new EtatValidationCompetence(StatutValidationCompetence.VALIDEE,
                NiveauCompetence.EXPERT, MethodeValidationCode.DOCUMENT, NOW.minusDays(1), null);
        assertThrows(IllegalStateException.class, () -> EffetDecisionValidation.appliquer(
                validee, DecisionValidation.REJETEE, null, MethodeValidationCode.DOCUMENT, NOW, null));
    }

    @Test
    void statutDeDemandeSuitLaDecision() {
        assertEquals(StatutDemandeValidation.VALIDEE, EffetDecisionValidation.statutDemandeApres(DecisionValidation.VALIDEE));
        assertEquals(StatutDemandeValidation.COMPLEMENT_REQUIS, EffetDecisionValidation.statutDemandeApres(DecisionValidation.COMPLEMENT_REQUIS));
    }
}
