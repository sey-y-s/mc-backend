package odk.malicompetences.backend.institution;

import odk.malicompetences.backend.besoin.BesoinCompetence;
import odk.malicompetences.backend.besoin.BesoinCompetenceRepository;
import odk.malicompetences.backend.besoin.StatutBesoin;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.territoire.NiveauTerritoire;
import odk.malicompetences.backend.territoire.Region;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Indicateurs offre / demande / écart calculés À LA DEMANDE (consignes V1.2 : pas de snapshots en V1).
 * <ul>
 *   <li><b>Offre</b> = nombre de citoyens DISTINCTS ayant la compétence VALIDEE et encore valable
 *       (valide_jusqu_au vide ou non dépassé), rattachés au territoire par leur commune de référence.</li>
 *   <li><b>Demande</b> = somme des personnes recherchées (quantité, 1 par défaut) dans les besoins
 *       OUVERTS et non expirés (date de fin vide ou non dépassée).</li>
 *   <li><b>Écart</b> = offre − demande (négatif : pénurie).</li>
 *   <li><b>Zones qui se chevauchent</b> : un besoin est compté UNE fois, dans sa zone la plus précise
 *       (commune → sa région ; sinon cercle → sa région ; sinon région ou district) ; sans zone, il n'est
 *       compté qu'au niveau national. Un citoyen sans commune n'est compté qu'au niveau national.</li>
 *   <li>Les totaux par compétence ne s'additionnent pas pour compter des personnes uniques.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class IndicateurCalculService {

    private final CitoyenCompetenceRepository citoyenCompetences;
    private final BesoinCompetenceRepository besoinCompetences;

    public IndicateurCalculService(CitoyenCompetenceRepository citoyenCompetences,
                                   BesoinCompetenceRepository besoinCompetences) {
        this.citoyenCompetences = citoyenCompetences;
        this.besoinCompetences = besoinCompetences;
    }

    /** Une cellule : compétence × territoire (territoire nul = niveau national). */
    public record Ligne(Competence competence, Region territoire, int offre, int demande) {
        public int ecart() {
            return offre - demande;
        }
    }

    public record Calcul(LocalDateTime calculeLe, List<Ligne> lignes) {
    }

    public static final String CRITERES = "Offre : citoyens distincts ayant la compétence validée et encore valable. "
            + "Demande : personnes recherchées dans les besoins ouverts et non expirés. Écart = offre − demande.";
    public static final String LIMITES = "Un besoin sans zone et un citoyen sans commune ne sont comptés qu'au niveau national. "
            + "Une compétence non renseignée n'est pas une compétence absente ; l'absence de diplôme n'est pas l'absence de savoir-faire.";

    public Calcul calculer(UUID competenceId) {
        LocalDate jour = LocalDate.now();
        return new Calcul(LocalDateTime.now(), agreger(
                citoyenCompetences.findByEtat(EtatCompetence.VALIDEE),
                besoinCompetences.findByBesoin_Statut(StatutBesoin.OUVERT),
                jour, competenceId));
    }

    /** Cœur du calcul, sans accès à la base (testable). */
    static List<Ligne> agreger(List<CitoyenCompetence> validees, List<BesoinCompetence> demandes,
                               LocalDate jour, UUID competenceId) {
        Map<UUID, Competence> competences = new LinkedHashMap<>();
        Map<UUID, Region> territoires = new HashMap<>();
        // clé : competenceId|territoireId (territoire vide = national)
        Map<String, Set<UUID>> offre = new HashMap<>();
        Map<String, Integer> demande = new HashMap<>();

        for (CitoyenCompetence cc : validees) {
            Competence c = cc.getCompetence();
            if (c == null || (competenceId != null && !competenceId.equals(c.getId()))) continue;
            if (cc.getEtat() != EtatCompetence.VALIDEE) continue;
            if (cc.getValideJusquau() != null && cc.getValideJusquau().isBefore(jour)) continue;
            competences.put(c.getId(), c);
            UUID personne = cc.getCitoyen().getId();
            offre.computeIfAbsent(cle(c, null), k -> new HashSet<>()).add(personne);
            Region r = cc.getCitoyen().getCommune() == null ? null : territoireRegional(cc.getCitoyen().getCommune().getRegion());
            if (r != null) {
                territoires.put(r.getId(), r);
                offre.computeIfAbsent(cle(c, r), k -> new HashSet<>()).add(personne);
            }
        }
        for (BesoinCompetence bc : demandes) {
            Competence c = bc.getCompetence();
            if (c == null || (competenceId != null && !competenceId.equals(c.getId()))) continue;
            var besoin = bc.getBesoin();
            if (besoin == null || besoin.getStatut() != StatutBesoin.OUVERT) continue;
            if (besoin.getDateFin() != null && besoin.getDateFin().isBefore(jour)) continue;
            int quantite = bc.getQuantite() == null || bc.getQuantite() < 1 ? 1 : bc.getQuantite();
            competences.put(c.getId(), c);
            demande.merge(cle(c, null), quantite, Integer::sum);
            Region r = zone(besoin);
            if (r != null) {
                territoires.put(r.getId(), r);
                demande.merge(cle(c, r), quantite, Integer::sum);
            }
        }

        List<Ligne> lignes = new ArrayList<>();
        for (Competence c : competences.values()) {
            lignes.add(new Ligne(c, null, taille(offre.get(cle(c, null))), demande.getOrDefault(cle(c, null), 0)));
            for (Region r : territoires.values()) {
                String k = cle(c, r);
                if (offre.containsKey(k) || demande.containsKey(k)) {
                    lignes.add(new Ligne(c, r, taille(offre.get(k)), demande.getOrDefault(k, 0)));
                }
            }
        }
        lignes.sort(Comparator.comparing((Ligne l) -> l.competence().getNom(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(l -> l.territoire() == null ? "" : l.territoire().getNom()));
        return lignes;
    }

    /** Zone la plus précise d'un besoin, ramenée à sa région (ou au district). */
    static Region zone(odk.malicompetences.backend.besoin.Besoin besoin) {
        if (besoin.getCommune() != null) return territoireRegional(besoin.getCommune().getRegion());
        return territoireRegional(besoin.getRegion());
    }

    /** Un cercle est ramené à sa région ; une région ou le district restent inchangés. */
    public static Region territoireRegional(Region r) {
        if (r == null) return null;
        if (r.getNiveau() == NiveauTerritoire.CERCLE) return r.getParent();
        return r;
    }

    private static String cle(Competence c, Region r) {
        return c.getId() + "|" + (r == null ? "" : r.getId());
    }

    private static int taille(Set<UUID> s) {
        return s == null ? 0 : s.size();
    }
}
