package odk.malicompetences.backend.common.regle;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AutresReglesTest {

    // ---- PorteeBesoin
    @Test
    void besoinSansZoneEstNationalEtNeCouvreAucuneCommune() {
        UUID commune = UUID.randomUUID();
        assertTrue(PorteeBesoin.estNational(List.of(), List.of()));
        assertFalse(PorteeBesoin.couvre(List.of(), List.of(), commune, Set.of(UUID.randomUUID())));
    }

    @Test
    void besoinZoneCouvreParCommuneOuParTerritoireAncetre() {
        UUID commune = UUID.randomUUID(), cercle = UUID.randomUUID(), region = UUID.randomUUID();
        assertTrue(PorteeBesoin.couvre(List.of(commune), List.of(), commune, Set.of(cercle, region)));
        assertTrue(PorteeBesoin.couvre(List.of(), List.of(region), commune, Set.of(cercle, region)));
        assertFalse(PorteeBesoin.couvre(List.of(), List.of(UUID.randomUUID()), commune, Set.of(cercle, region)));
    }

    // ---- MasquageStatistique
    @Test
    void petitesCellulesEtCelluleComplementaireSontMasquees() {
        Map<String, Integer> m = new HashMap<>();
        m.put("A", 2); m.put("B", 8); m.put("C", 20); m.put("D", null);
        // A (<5) et D (inconnu) masquees ; B, la plus petite visible, l'est aussi (masquage complementaire)
        assertEquals(Set.of("A", "B", "D"), MasquageStatistique.codesMasques(m, 5));
    }

    @Test
    void rienEstMasqueSiToutesLesCellulesDepassentLeSeuil() {
        assertEquals(Set.of(), MasquageStatistique.codesMasques(Map.of("A", 6, "B", 9), 5));
    }

    @Test
    void seuilInvalideEstRefuse() {
        assertThrows(IllegalArgumentException.class, () -> MasquageStatistique.codesMasques(Map.of("A", 6), 0));
    }

    // ---- ReglesLocalisation
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 22, 10, 0);
    private final UUID ref = UUID.randomUUID();
    private final UUID autre = UUID.randomUUID();
    private final Duration delai = Duration.ofDays(30);

    @Test
    void sansConsentementOnNeProposeRien() {
        assertEquals(ReglesLocalisation.Decision.GPS_NON_CONSENTI,
                ReglesLocalisation.evaluer(false, false, ref, autre, null, NOW, delai));
    }

    @Test
    void memeCommuneOuCommuneInconnueNeDeclenchePasDeDemande() {
        assertEquals(ReglesLocalisation.Decision.RIEN_A_FAIRE, ReglesLocalisation.evaluer(true, false, ref, ref, null, NOW, delai));
        assertEquals(ReglesLocalisation.Decision.COMMUNE_INCONNUE, ReglesLocalisation.evaluer(true, false, ref, null, null, NOW, delai));
    }

    @Test
    void uneSeuleDemandeEnAttenteEtDelaiApresReponse() {
        assertEquals(ReglesLocalisation.Decision.DEJA_EN_ATTENTE, ReglesLocalisation.evaluer(true, true, ref, autre, null, NOW, delai));
        assertEquals(ReglesLocalisation.Decision.DELAI_NON_ECOULE,
                ReglesLocalisation.evaluer(true, false, ref, autre, NOW.minusDays(10), NOW, delai));
        assertEquals(ReglesLocalisation.Decision.PROPOSER,
                ReglesLocalisation.evaluer(true, false, ref, autre, NOW.minusDays(31), NOW, delai));
        assertEquals(ReglesLocalisation.Decision.PROPOSER, ReglesLocalisation.evaluer(true, false, ref, autre, null, NOW, delai));
    }

    // ---- ReglesMiseEnRelation
    @Test
    void delaiApresRefusDeTrenteJours() {
        assertFalse(ReglesMiseEnRelation.peutRedemanderApresRefus(NOW.minusDays(29), NOW, ReglesMiseEnRelation.DELAI_APRES_REFUS));
        assertTrue(ReglesMiseEnRelation.peutRedemanderApresRefus(NOW.minusDays(30), NOW, ReglesMiseEnRelation.DELAI_APRES_REFUS));
        assertTrue(ReglesMiseEnRelation.peutRedemanderApresRefus(null, NOW, ReglesMiseEnRelation.DELAI_APRES_REFUS));
    }
}
