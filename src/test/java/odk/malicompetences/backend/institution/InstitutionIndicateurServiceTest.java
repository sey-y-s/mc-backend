package odk.malicompetences.backend.institution;

import odk.malicompetences.backend.common.regle.MasquageStatistique;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class InstitutionIndicateurServiceTest {
    @Test
    void masqueEffectifFaibleEtUneCelluleComplementaire() {
        Set<String> masques = MasquageStatistique.codesMasques(Map.of("ML-01", 3, "ML-02", 15, "ML-03", 20), 5);
        assertEquals(Set.of("ML-01", "ML-02"), masques);
    }

    @Test
    void neMasquePasQuandToutesLesCellulesDepassentLeSeuil() {
        Set<String> masques = MasquageStatistique.codesMasques(Map.of("ML-01", 5, "ML-02", 15), 5);
        assertTrue(masques.isEmpty());
    }
}
