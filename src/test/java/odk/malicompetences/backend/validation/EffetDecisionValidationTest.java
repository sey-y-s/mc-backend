package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.common.regle.EffetDecisionValidation;
import odk.malicompetences.backend.common.regle.EtatValidationCompetence;
import odk.malicompetences.backend.common.statut.DecisionValidation;
import odk.malicompetences.backend.common.statut.MethodeValidationCode;
import odk.malicompetences.backend.common.statut.StatutDemandeValidation;
import odk.malicompetences.backend.common.statut.StatutValidationCompetence;
import odk.malicompetences.backend.competence.NiveauCompetence;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EffetDecisionValidationTest {
 @Test void validationReussieProduitUnEtatValide(){
  var maintenant=LocalDateTime.of(2026,10,24,12,0);
  var resultat=EffetDecisionValidation.appliquer(EtatValidationCompetence.initial(),DecisionValidation.VALIDEE,NiveauCompetence.EXPERT,MethodeValidationCode.TEST_NUMERIQUE,maintenant,12);
  assertEquals(StatutValidationCompetence.VALIDEE,resultat.statut()); assertEquals(NiveauCompetence.EXPERT,resultat.niveauValide());
  assertEquals(maintenant,resultat.valideeLe()); assertEquals(maintenant.toLocalDate().plusMonths(12),resultat.valideJusquAu());
 }
 @Test void rejetClotureLaDemandeSansEffacerLaValidationPrecedente(){
  var date=LocalDateTime.of(2025,1,1,0,0);
  var precedent=new EtatValidationCompetence(StatutValidationCompetence.EN_COURS,NiveauCompetence.INTERMEDIAIRE,MethodeValidationCode.DOCUMENT,date,null);
  var resultat=EffetDecisionValidation.appliquer(precedent,DecisionValidation.REJETEE,null,MethodeValidationCode.DOCUMENT,LocalDateTime.now(),null);
  assertEquals(StatutValidationCompetence.REJETEE,resultat.statut()); assertEquals(precedent.niveauValide(),resultat.niveauValide()); assertEquals(date,resultat.valideeLe());
  assertEquals(StatutDemandeValidation.REJETEE,EffetDecisionValidation.statutDemandeApres(DecisionValidation.REJETEE));
 }
}
