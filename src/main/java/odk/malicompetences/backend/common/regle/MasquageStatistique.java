package odk.malicompetences.backend.common.regle;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Protection des petits effectifs pour toute statistique publique (reprise de la logique de
 * InstitutionIndicateurService). L'effectif a comparer au seuil est le nombre de PERSONNES DISTINCTES
 * du groupe, pas l'offre ni la demande.
 *
 * <ul>
 *   <li>Une cellule dont l'effectif est inconnu ou inferieur au seuil est masquee.</li>
 *   <li>Si au moins une cellule est masquee, la plus petite cellule visible l'est aussi (masquage
 *       complementaire) : sinon un total affiche permettrait de retrouver la cellule masquee par soustraction.</li>
 * </ul>
 */
public final class MasquageStatistique {

    public static final int SEUIL_PAR_DEFAUT = 5;

    private MasquageStatistique() {}

    /** @param effectifParCellule code de cellule (ex. code territoire) -> effectif de personnes distinctes (null = inconnu) */
    public static Set<String> codesMasques(Map<String, Integer> effectifParCellule, int seuil) {
        if (seuil < 1) throw new IllegalArgumentException("Le seuil doit etre >= 1");
        Set<String> masques = new TreeSet<>();
        Map<String, Integer> ordonne = new TreeMap<>(effectifParCellule);
        ordonne.forEach((code, effectif) -> {
            if (effectif == null || effectif < seuil) masques.add(code);
        });
        if (!masques.isEmpty()) {
            ordonne.entrySet().stream()
                    .filter(e -> !masques.contains(e.getKey()))
                    .min(Map.Entry.comparingByValue())
                    .ifPresent(e -> masques.add(e.getKey()));
        }
        return masques;
    }
}
