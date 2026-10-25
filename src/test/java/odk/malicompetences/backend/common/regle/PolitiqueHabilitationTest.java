package odk.malicompetences.backend.common.regle;

import odk.malicompetences.backend.common.regle.PolitiqueHabilitation.DemandeInfo;
import odk.malicompetences.backend.common.regle.PolitiqueHabilitation.HabilitationInfo;
import odk.malicompetences.backend.common.regle.PolitiqueHabilitation.Refus;
import odk.malicompetences.backend.common.statut.MethodeValidationCode;
import odk.malicompetences.backend.common.statut.StatutCentre;
import odk.malicompetences.backend.common.statut.StatutEvaluateur;
import odk.malicompetences.backend.common.statut.StatutHabilitation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PolitiqueHabilitationTest {

    private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 10, 22);
    private final UUID evaluateurCitoyen = UUID.randomUUID();
    private final UUID demandeur = UUID.randomUUID();
    private final UUID competence = UUID.randomUUID();
    private final UUID centre = UUID.randomUUID();

    private HabilitationInfo hab(MethodeValidationCode m, UUID centreId, LocalDate debut, LocalDate fin, StatutHabilitation s) {
        return new HabilitationInfo(evaluateurCitoyen, StatutEvaluateur.ACTIF, s, competence, m, centreId, debut, fin);
    }

    private DemandeInfo dem(MethodeValidationCode m, UUID centreId) {
        return new DemandeInfo(demandeur, competence, m, centreId);
    }

    @Test
    void maitreArtisanHabiliteSansCentreEstAutorise() {
        assertEquals(Optional.empty(), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.MAITRE_ARTISAN, null, null, null, StatutHabilitation.ACTIVE),
                dem(MethodeValidationCode.MAITRE_ARTISAN, null), AUJOURDHUI));
    }

    @Test
    void habilitationExpireeOuPasCommenceeEstRefusee() {
        assertEquals(Optional.of(Refus.HABILITATION_EXPIREE), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.MAITRE_ARTISAN, null, null, AUJOURDHUI.minusDays(1), StatutHabilitation.ACTIVE),
                dem(MethodeValidationCode.MAITRE_ARTISAN, null), AUJOURDHUI));
        assertEquals(Optional.of(Refus.HABILITATION_NON_COMMENCEE), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.MAITRE_ARTISAN, null, AUJOURDHUI.plusDays(1), null, StatutHabilitation.ACTIVE),
                dem(MethodeValidationCode.MAITRE_ARTISAN, null), AUJOURDHUI));
    }

    @Test
    void jourDeFinEstInclus() {
        assertEquals(Optional.empty(), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.MAITRE_ARTISAN, null, null, AUJOURDHUI, StatutHabilitation.ACTIVE),
                dem(MethodeValidationCode.MAITRE_ARTISAN, null), AUJOURDHUI));
    }

    @Test
    void habilitationSuspendueOuEvaluateurInactifEstRefuse() {
        assertEquals(Optional.of(Refus.HABILITATION_INACTIVE), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.MAITRE_ARTISAN, null, null, null, StatutHabilitation.SUSPENDUE),
                dem(MethodeValidationCode.MAITRE_ARTISAN, null), AUJOURDHUI));
        HabilitationInfo evalSuspendu = new HabilitationInfo(evaluateurCitoyen, StatutEvaluateur.SUSPENDU,
                StatutHabilitation.ACTIVE, competence, MethodeValidationCode.MAITRE_ARTISAN, null, null, null);
        assertEquals(Optional.of(Refus.EVALUATEUR_INACTIF), PolitiqueHabilitation.verifier(
                evalSuspendu, dem(MethodeValidationCode.MAITRE_ARTISAN, null), AUJOURDHUI));
    }

    @Test
    void mauvaiseCompetenceOuMethodeEstRefusee() {
        assertEquals(Optional.of(Refus.METHODE_NON_COUVERTE), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.MAITRE_ARTISAN, null, null, null, StatutHabilitation.ACTIVE),
                dem(MethodeValidationCode.PORTFOLIO, null), AUJOURDHUI));
        DemandeInfo autreCompetence = new DemandeInfo(demandeur, UUID.randomUUID(), MethodeValidationCode.MAITRE_ARTISAN, null);
        assertEquals(Optional.of(Refus.COMPETENCE_NON_COUVERTE), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.MAITRE_ARTISAN, null, null, null, StatutHabilitation.ACTIVE), autreCompetence, AUJOURDHUI));
    }

    @Test
    void onNeSEvalueJamaisSoiMeme() {
        DemandeInfo soiMeme = new DemandeInfo(evaluateurCitoyen, competence, MethodeValidationCode.MAITRE_ARTISAN, null);
        assertEquals(Optional.of(Refus.AUTO_EVALUATION), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.MAITRE_ARTISAN, null, null, null, StatutHabilitation.ACTIVE), soiMeme, AUJOURDHUI));
    }

    @Test
    void evaluationEnCentreExigeLeBonCentre() {
        assertEquals(Optional.of(Refus.CENTRE_REQUIS), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.EVALUATION_CENTRE, centre, null, null, StatutHabilitation.ACTIVE),
                dem(MethodeValidationCode.EVALUATION_CENTRE, null), AUJOURDHUI));
        assertEquals(Optional.of(Refus.CENTRE_NON_COUVERT), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.EVALUATION_CENTRE, centre, null, null, StatutHabilitation.ACTIVE),
                dem(MethodeValidationCode.EVALUATION_CENTRE, UUID.randomUUID()), AUJOURDHUI));
        assertEquals(Optional.of(Refus.CENTRE_NON_COUVERT), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.EVALUATION_CENTRE, null, null, null, StatutHabilitation.ACTIVE),
                dem(MethodeValidationCode.EVALUATION_CENTRE, centre), AUJOURDHUI));
        assertEquals(Optional.empty(), PolitiqueHabilitation.verifier(
                hab(MethodeValidationCode.EVALUATION_CENTRE, centre, null, null, StatutHabilitation.ACTIVE),
                dem(MethodeValidationCode.EVALUATION_CENTRE, centre), AUJOURDHUI));
    }

    @Test
    void centreDoitEtreActifEtAgree() {
        assertEquals(Optional.of(Refus.CENTRE_INACTIF), PolitiqueHabilitation.verifierCentre(StatutCentre.SUSPENDU, true));
        assertEquals(Optional.of(Refus.CENTRE_NON_AGREE), PolitiqueHabilitation.verifierCentre(StatutCentre.ACTIF, false));
        assertEquals(Optional.empty(), PolitiqueHabilitation.verifierCentre(StatutCentre.ACTIF, true));
    }
}
