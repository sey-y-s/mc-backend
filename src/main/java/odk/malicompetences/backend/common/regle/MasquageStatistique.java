package odk.malicompetences.backend.common.regle;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Protection des petits effectifs pour toute statistique publique.
 *
 * <p>L'effectif comparé au seuil est le nombre de PERSONNES DISTINCTES d'une cellule (pas l'offre brute).
 * <ul>
 *   <li>Une cellule vide (0) n'identifie personne : elle reste visible.</li>
 *   <li>Une cellule de 1 à {@code seuil - 1} personnes est masquée.</li>
 *   <li>Si une cellule est masquée, la plus petite cellule visible non vide l'est aussi (masquage complémentaire) :
 *       sans cela, un total affiché permettrait de retrouver la cellule masquée par soustraction.</li>
 * </ul>
 * Règle pure : aucune dépendance à Spring ni à la base, donc testable seule.
 */
public final class MasquageStatistique {

    public static final int SEUIL_PAR_DEFAUT = 5;

    private MasquageStatistique() {}

    /**
     * @param effectifParCellule effectif de personnes distinctes par cellule. L'ORDRE D'ITÉRATION départage les
     *                           égalités du masquage complémentaire : passer une map ordonnée (ex. par code stable).
     * @return les cellules à masquer
     */
    public static <K> Set<K> cellulesMasquees(Map<K, Integer> effectifParCellule, int seuil) {
        if (seuil < 1) throw new IllegalArgumentException("Le seuil doit être >= 1");
        Set<K> masquees = new HashSet<>();
        effectifParCellule.forEach((cellule, effectif) -> {
            if (effectif > 0 && effectif < seuil) masquees.add(cellule);
        });
        if (masquees.isEmpty()) return masquees;

        K complementaire = null;
        int plusPetit = Integer.MAX_VALUE;
        for (Map.Entry<K, Integer> e : effectifParCellule.entrySet()) {
            if (masquees.contains(e.getKey()) || e.getValue() <= 0) continue;
            if (e.getValue() < plusPetit) {
                plusPetit = e.getValue();
                complementaire = e.getKey();
            }
        }
        if (complementaire != null) masquees.add(complementaire);
        return masquees;
    }
}
