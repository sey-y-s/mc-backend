package odk.malicompetences.backend.geolocalisation;

import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.talent.TalentMatchingResponse;
import odk.malicompetences.backend.talent.TalentRechercheService;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.Region;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class GeolocalisationService {

    /** Centre du Mali, utilisé seulement si aucune coordonnée n'est connue pour une région. */
    private static final double MALI_LAT = 17.57;
    private static final double MALI_LNG = -3.99;

    private final CitoyenRepository citoyenRepository;
    private final OrganisationRepository organisationRepository;
    private final BesoinRepository besoinRepository;
    private final TalentRechercheService talentRechercheService;

    public GeolocalisationService(CitoyenRepository citoyenRepository,
                                  OrganisationRepository organisationRepository,
                                  BesoinRepository besoinRepository,
                                  TalentRechercheService talentRechercheService) {
        this.citoyenRepository = citoyenRepository;
        this.organisationRepository = organisationRepository;
        this.besoinRepository = besoinRepository;
        this.talentRechercheService = talentRechercheService;
    }

    // Citoyen : aucune coordonnée n'est conservée (consignes V1.2). La localisation d'un citoyen
    // est sa commune de référence, gérée par LocalisationCitoyenService.

    // ---------- Organisation ----------

    @Transactional(readOnly = true)
    public PositionResponse positionOrganisation(UUID organisationId) {
        Organisation o = organisation(organisationId);
        return PositionResponse.of(o.getLatitude(), o.getLongitude(), o.getDatePosition());
    }

    public PositionResponse definirPositionOrganisation(UUID organisationId, PositionRequest request) {
        Organisation o = organisation(organisationId);
        o.setLatitude(request.latitude());
        o.setLongitude(request.longitude());
        o.setDatePosition(LocalDateTime.now());
        organisationRepository.save(o);
        return PositionResponse.of(o.getLatitude(), o.getLongitude(), o.getDatePosition());
    }

    public void effacerPositionOrganisation(UUID organisationId) {
        Organisation o = organisation(organisationId);
        o.setLatitude(null);
        o.setLongitude(null);
        o.setDatePosition(null);
        organisationRepository.save(o);
    }

    // ---------- Lieu d'une demande (besoin) ----------

    public CarteTalentsResponse.LieuDemande definirLieuBesoin(UUID besoinId, PositionRequest request,
                                                              UUID utilisateurId, boolean administrateur) {
        Besoin besoin = besoinAutorise(besoinId, utilisateurId, administrateur);
        besoin.setLatitude(request.latitude());
        besoin.setLongitude(request.longitude());
        besoinRepository.save(besoin);
        return lieu(besoin);
    }

    public CarteTalentsResponse.LieuDemande effacerLieuBesoin(UUID besoinId, UUID utilisateurId, boolean administrateur) {
        Besoin besoin = besoinAutorise(besoinId, utilisateurId, administrateur);
        besoin.setLatitude(null);
        besoin.setLongitude(null);
        besoinRepository.save(besoin);
        return lieu(besoin);
    }

    // ---------- Carte des talents par localité ----------

    @Transactional(readOnly = true)
    public CarteTalentsResponse carteTalents(UUID besoinId, UUID utilisateurId, boolean administrateur) {
        Besoin besoin = besoinAutorise(besoinId, utilisateurId, administrateur);
        CarteTalentsResponse.LieuDemande lieu = lieu(besoin);

        // Même calcul que le matching : seuls les talents qui ont au moins une compétence demandée.
        List<TalentMatchingResponse> correspondances = talentRechercheService
                .correspondances(besoinId, utilisateurId, administrateur).stream()
                .filter(m -> m.score() > 0)
                .toList();

        Map<String, Groupe> groupes = new LinkedHashMap<>();
        int sansLocalite = 0;
        for (TalentMatchingResponse m : correspondances) {
            Citoyen c = citoyenRepository.findById(m.talentId()).orElse(null);
            if (c == null) continue;
            Commune commune = c.getCommune();
            Region region = commune != null ? commune.getRegion() : null;
            if (commune == null) {
                sansLocalite++;
                continue;
            }
            String cle = "commune:" + commune.getId();
            Groupe g = groupes.computeIfAbsent(cle, k -> new Groupe(commune, region));
            g.ajouter(c, m);
        }

        List<CarteTalentsResponse.Localite> localites = new ArrayList<>();
        for (Map.Entry<String, Groupe> e : groupes.entrySet()) {
            localites.add(e.getValue().versLocalite(e.getKey(), lieu));
        }
        localites.sort(Comparator
                .comparing((CarteTalentsResponse.Localite l) -> l.distanceKm() == null ? Double.MAX_VALUE : l.distanceKm())
                .thenComparing(CarteTalentsResponse.Localite::nombreTalents, Comparator.reverseOrder()));

        int total = localites.stream().mapToInt(CarteTalentsResponse.Localite::nombreTalents).sum() + sansLocalite;
        return new CarteTalentsResponse(besoin.getId(), besoin.getTitre(), lieu, total, sansLocalite, localites);
    }

    // ---------- Carte du Mali : tous les talents par localité ----------

    /** Recense tous les talents (filtres facultatifs) et les regroupe par commune ; totaux par région. */
    @Transactional(readOnly = true)
    public CarteGeneraleResponse carteGenerale(odk.malicompetences.backend.talent.TalentFiltres filtres) {
        List<odk.malicompetences.backend.talent.TalentRechercheResponse> talents =
                talentRechercheService.rechercher(filtres, null, null);

        Map<String, GroupeGeneral> groupes = new LinkedHashMap<>();
        Map<UUID, int[]> parRegion = new LinkedHashMap<>();
        Map<UUID, Region> regionsVues = new LinkedHashMap<>();
        int sansLocalite = 0;
        int disponibles = 0;
        int valides = 0;
        for (var t : talents) {
            Citoyen c = citoyenRepository.findById(t.id()).orElse(null);
            if (c == null) continue;
            boolean valide = t.competences().stream().anyMatch(odk.malicompetences.backend.talent.TalentCompetenceResponse::validee);
            if (t.estDisponible()) disponibles++;
            if (valide) valides++;
            Commune commune = c.getCommune();
            Region region = commune != null ? commune.getRegion() : null;
            if (commune == null) {
                sansLocalite++;
                continue;
            }
            if (region != null) {
                regionsVues.put(region.getId(), region);
                int[] r = parRegion.computeIfAbsent(region.getId(), k -> new int[2]);
                r[0]++;
                if (t.estDisponible()) r[1]++;
            }
            String cle = "commune:" + commune.getId();
            groupes.computeIfAbsent(cle, k -> new GroupeGeneral(commune, region)).ajouter(c, t, valide);
        }

        List<CarteGeneraleResponse.LocaliteGenerale> localites = new ArrayList<>();
        for (Map.Entry<String, GroupeGeneral> e : groupes.entrySet()) {
            localites.add(e.getValue().versLocalite(e.getKey()));
        }
        localites.sort(Comparator.comparingInt(CarteGeneraleResponse.LocaliteGenerale::nombreTalents).reversed());

        List<CarteGeneraleResponse.RegionTalents> regions = new ArrayList<>();
        for (Map.Entry<UUID, int[]> e : parRegion.entrySet()) {
            Region r = regionsVues.get(e.getKey());
            regions.add(new CarteGeneraleResponse.RegionTalents(r.getId(), r.getNom(), r.getCodeStable(), e.getValue()[0], e.getValue()[1]));
        }
        regions.sort(Comparator.comparingInt(CarteGeneraleResponse.RegionTalents::nombreTalents).reversed());

        return new CarteGeneraleResponse(talents.size(), disponibles, valides, sansLocalite, regions, localites);
    }

    /**
     * Position affichée d'une localité : position de référence de la commune (référentiel),
     * sinon chef-lieu de la région légèrement décalé. Jamais la position d'une personne.
     */
    static double[] positionLocalite(Commune commune, Region region) {
        if (commune != null && commune.getLatitude() != null && commune.getLongitude() != null) {
            return new double[]{commune.getLatitude(), commune.getLongitude()};
        }
        double lat = region != null && region.getLatitude() != null ? region.getLatitude() : MALI_LAT;
        double lng = region != null && region.getLongitude() != null ? region.getLongitude() : MALI_LNG;
        if (commune != null) {
            int h = Math.abs(commune.getId().hashCode());
            double angle = (h % 360) * Math.PI / 180.0;
            double rayon = 0.04 + (h % 7) * 0.012;
            lat = arrondi(lat + rayon * Math.sin(angle), 4);
            lng = arrondi(lng + rayon * Math.cos(angle), 4);
        }
        return new double[]{lat, lng};
    }

    private static boolean positionCommuneConnue(Commune commune) {
        return commune != null && commune.getLatitude() != null && commune.getLongitude() != null;
    }

    private static final class GroupeGeneral {
        private final Commune commune;
        private final Region region;
        private int talents;
        private int disponibles;
        private int valides;
        private final Map<String, Integer> competences = new java.util.HashMap<>();
        private final Map<String, Integer> metiers = new java.util.HashMap<>();

        GroupeGeneral(Commune commune, Region region) {
            this.commune = commune;
            this.region = region;
        }

        void ajouter(Citoyen c, odk.malicompetences.backend.talent.TalentRechercheResponse t, boolean valide) {
            talents++;
            if (t.estDisponible()) disponibles++;
            if (valide) valides++;
            for (var comp : t.competences()) {
                if (comp.nom() != null) competences.merge(comp.nom(), 1, Integer::sum);
            }
            if (t.metierNom() != null) metiers.merge(t.metierNom(), 1, Integer::sum);
        }

        private static List<String> top(Map<String, Integer> valeurs) {
            return valeurs.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .toList();
        }

        CarteGeneraleResponse.LocaliteGenerale versLocalite(String cle) {
            double[] p = positionLocalite(commune, region);
            return new CarteGeneraleResponse.LocaliteGenerale(
                    cle,
                    commune != null ? commune.getId() : null,
                    commune != null ? commune.getNom() : "Commune non renseignée",
                    region != null ? region.getId() : null,
                    region != null ? region.getNom() : null,
                    p[0], p[1], positionCommuneConnue(commune), talents, disponibles, valides,
                    top(competences), top(metiers));
        }
    }

    // ---------- Outils ----------

    private CarteTalentsResponse.LieuDemande lieu(Besoin besoin) {
        if (besoin.getLatitude() != null && besoin.getLongitude() != null) {
            return new CarteTalentsResponse.LieuDemande(besoin.getLatitude(), besoin.getLongitude(), "BESOIN");
        }
        Organisation o = besoin.getOrganisation();
        if (o != null && o.getLatitude() != null && o.getLongitude() != null) {
            return new CarteTalentsResponse.LieuDemande(o.getLatitude(), o.getLongitude(), "ORGANISATION");
        }
        return new CarteTalentsResponse.LieuDemande(null, null, "AUCUN");
    }

    private Besoin besoinAutorise(UUID besoinId, UUID utilisateurId, boolean administrateur) {
        Besoin besoin = besoinRepository.findById(besoinId)
                .orElseThrow(() -> new ResourceNotFoundException("Besoin introuvable."));
        if (!administrateur && (besoin.getOrganisation() == null || !besoin.getOrganisation().getId().equals(utilisateurId))) {
            throw new AccessDeniedException("Accès refusé à ce besoin.");
        }
        return besoin;
    }

    private Organisation organisation(UUID id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation introuvable."));
    }

    static double arrondi(double valeur, int decimales) {
        double f = Math.pow(10, decimales);
        return Math.round(valeur * f) / f;
    }

    static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** Regroupe les talents d'une même commune ; la position affichée est celle de la commune (référentiel). */
    private static final class Groupe {
        private final Commune commune;
        private final Region region;
        private final List<UUID> talentIds = new ArrayList<>();
        private int disponibles;
        private int meilleurScore;
        private long sommeScores;

        Groupe(Commune commune, Region region) {
            this.commune = commune;
            this.region = region;
        }

        void ajouter(Citoyen c, TalentMatchingResponse m) {
            talentIds.add(c.getId());
            if (m.talent() != null && m.talent().estDisponible()) disponibles++;
            meilleurScore = Math.max(meilleurScore, m.score());
            sommeScores += m.score();
        }

        CarteTalentsResponse.Localite versLocalite(String cle, CarteTalentsResponse.LieuDemande lieu) {
            double[] p = positionLocalite(commune, region);
            double lat = p[0];
            double lng = p[1];
            boolean mesuree = positionCommuneConnue(commune);
            Double distance = lieu.latitude() == null ? null
                    : arrondi(distanceKm(lieu.latitude(), lieu.longitude(), lat, lng), 1);
            int n = talentIds.size();
            return new CarteTalentsResponse.Localite(
                    cle,
                    commune != null ? commune.getId() : null,
                    commune != null ? commune.getNom() : "Commune non renseignée",
                    region != null ? region.getId() : null,
                    region != null ? region.getNom() : null,
                    lat, lng, mesuree, n, disponibles, meilleurScore,
                    n == 0 ? 0 : (int) Math.round((double) sommeScores / n),
                    distance, List.copyOf(talentIds));
        }
    }
}
