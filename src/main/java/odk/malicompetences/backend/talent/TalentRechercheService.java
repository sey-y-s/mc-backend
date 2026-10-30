package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.besoin.BesoinCompetence;
import odk.malicompetences.backend.besoin.BesoinCompetenceRepository;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.citoyen.ProfilCompletudeService;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.competence.NiveauCompetence;
import odk.malicompetences.backend.portfolio.PortfolioRealisationRepository;
import odk.malicompetences.backend.territoire.Commune;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TalentRechercheService {

    private final CitoyenRepository citoyenRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final BesoinRepository besoinRepository;
    private final BesoinCompetenceRepository besoinCompetenceRepository;
    private final PortfolioRealisationRepository portfolioRealisationRepository;
        private final ProfilCompletudeService profilCompletudeService;
    private final odk.malicompetences.backend.miseenrelation.DemandeMiseEnRelationRepository relations;
        private final odk.malicompetences.backend.portfolio.PortfolioRealisationMapper portfolioMapper;
    private final odk.malicompetences.backend.organisation.OrganisationRepository organisations;

    public TalentRechercheService(
            CitoyenRepository citoyenRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            BesoinRepository besoinRepository,
            BesoinCompetenceRepository besoinCompetenceRepository,
            PortfolioRealisationRepository portfolioRealisationRepository,
                        ProfilCompletudeService profilCompletudeService,
            odk.malicompetences.backend.miseenrelation.DemandeMiseEnRelationRepository relations,
                        odk.malicompetences.backend.portfolio.PortfolioRealisationMapper portfolioMapper,
            odk.malicompetences.backend.organisation.OrganisationRepository organisations
    ) {
        this.citoyenRepository = citoyenRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.besoinRepository = besoinRepository;
        this.besoinCompetenceRepository = besoinCompetenceRepository;
        this.portfolioRealisationRepository = portfolioRealisationRepository;
                this.profilCompletudeService = profilCompletudeService;
        this.relations = relations;
                this.portfolioMapper = portfolioMapper;
        this.organisations = organisations;
    }

    public List<TalentRechercheResponse> rechercher(
            String competence,
            String metier,
            String localisation,
            String disponibilite
    ) {
        return rechercher(new TalentFiltres(competence, metier, localisation, disponibilite,
                null, null, null, null, null, null), null, null);
    }

    /** Recherche filtrée ; si {@code size} est fourni, renvoie la page demandée (page commence à 0). */
        public List<TalentRechercheResponse> rechercher(TalentFiltres filtres, Integer page, Integer size) {
        return rechercher(filtres, page, size, null);
    }

    /**
     * Recherche faite par un citoyen : il ne se trouve jamais lui-même, et un profil incomplet ne peut pas chercher
     * (il n'est pas visible des autres, il ne voit pas non plus les autres).
     */
        public List<TalentRechercheResponse> rechercher(TalentFiltres filtres, Integer page, Integer size, UUID chercheurCitoyenId) {
        return rechercher(filtres, page, size, chercheurCitoyenId, null);
    }

    /** utilisateurId : compte qui cherche (citoyen ou organisation) ; ses contacts établis ne sont plus anonymes. */
    public List<TalentRechercheResponse> rechercher(TalentFiltres filtres, Integer page, Integer size,
                                                    UUID chercheurCitoyenId, UUID utilisateurId) {
                exigerOrganisationApprouvee(utilisateurId);
        java.util.Set<UUID> contacts = contacts(utilisateurId);
        if (chercheurCitoyenId != null) {
            exigerChercheurComplet(chercheurCitoyenId);
        }
        List<Citoyen> trouves = citoyenRepository.findAll().stream()
                .filter(citoyen -> chercheurCitoyenId == null || !citoyen.getId().equals(chercheurCitoyenId))
                .filter(profilCompletudeService::estVisible)
                .filter(citoyen -> matches(citoyen, filtres))
                .sorted(Comparator.comparing(citoyen -> citoyen.getId().toString()))
                .toList();
        if (size != null && size > 0) {
            int debut = Math.min(Math.max(page == null ? 0 : page, 0) * size, trouves.size());
            int fin = Math.min(debut + size, trouves.size());
            trouves = trouves.subList(debut, fin);
        }
                return trouves.stream().map(c -> toResponse(c, contacts)).toList();
    }

        /** Un citoyen dont le profil est incomplet ne peut pas rechercher de talents. */
    public void exigerChercheurComplet(UUID citoyenId) {
        citoyenRepository.findById(citoyenId).ifPresent(chercheur -> {
            if (!profilCompletudeService.estVisible(chercheur)) {
                throw new IllegalStateException("Complétez votre passeport (photo, commune, compétence avec preuve) "
                        + "pour rechercher des talents.");
            }
        });
    }

        public TalentRechercheResponse trouverParId(UUID id, UUID chercheurCitoyenId) {
        return trouverParId(id, chercheurCitoyenId, null);
    }

        public TalentRechercheResponse trouverParId(UUID id, UUID chercheurCitoyenId, UUID utilisateurId) {
        exigerOrganisationApprouvee(utilisateurId);
        if (chercheurCitoyenId != null) {
            if (chercheurCitoyenId.equals(id)) {
                throw new ResourceNotFoundException("Profil talent introuvable.");
            }
            exigerChercheurComplet(chercheurCitoyenId);
        }
                Citoyen citoyen = citoyenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profil talent introuvable."));
        profilCompletudeService.exigerVisible(citoyen);
        return toResponse(citoyen, contacts(utilisateurId));
    }

    /**
     * Profil complet d'une personne avec qui une mise en relation a été acceptée. Rien n'est révélé à un tiers :
     * sans mise en relation acceptée (ou sans être administrateur), le profil n'existe pas.
     */
    public ProfilContactResponse profilDuContact(UUID talentId, UUID moi, boolean administrateur) {
        Citoyen citoyen = citoyenRepository.findById(talentId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil introuvable."));
        if (!administrateur && !contacts(moi).contains(talentId)) {
            throw new ResourceNotFoundException("Profil introuvable.");
        }
        Commune commune = citoyen.getCommune();
        return new ProfilContactResponse(citoyen.getId(), citoyen.getPrenom(), citoyen.getNom(), citoyen.getPhotoUrl(),
                commune == null ? null : commune.getNom(),
                commune == null ? null : commune.getRegion().getNom(),
                citoyen.getDisponibilite(),
                toResponse(citoyen, java.util.Set.of(talentId)).competences(),
                portfolioRealisationRepository.findByCitoyenId(talentId).stream().map(portfolioMapper::toResponse).toList());
    }

        /**
     * Une organisation doit être approuvée par l'administration avant de chercher ou de contacter des citoyens.
     * Sans ligne « organisation » (citoyen, administrateur), aucune restriction.
     */
    public void exigerOrganisationApprouvee(UUID utilisateurId) {
        if (utilisateurId == null) {
            return;
        }
        organisations.findById(utilisateurId).ifPresent(organisation -> {
            if (organisation.getStatut() != odk.malicompetences.backend.organisation.StatutOrganisation.APPROUVEE) {
                throw new IllegalStateException("Votre organisation n'est pas encore approuvée : vous pourrez rechercher et contacter des talents dès son approbation.");
            }
        });
    }

    private java.util.Set<UUID> contacts(UUID utilisateurId) {
        return utilisateurId == null ? java.util.Set.of() : relations.contactsEtablis(utilisateurId);
    }


    public TalentRechercheResponse trouverParId(UUID id) {
        Citoyen citoyen = citoyenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profil talent introuvable."));
        profilCompletudeService.exigerVisible(citoyen);
        return toResponse(citoyen);
    }

    public List<TalentMatchingResponse> correspondances(UUID besoinId, UUID organisationId, boolean administrateur) {
        Besoin besoin = besoinRepository.findById(besoinId)
                .orElseThrow(() -> new ResourceNotFoundException("Besoin introuvable."));
        if (!administrateur && !besoin.getOrganisation().getId().equals(organisationId)) {
            throw new AccessDeniedException("Accès refusé à ce besoin.");
        }
        List<BesoinCompetence> besoinsCompetences = besoinCompetenceRepository.findByBesoinId(besoinId);
        if (besoinsCompetences == null || besoinsCompetences.isEmpty()) {
            return List.of();
        }

        return citoyenRepository.findAll().stream()
                .filter(profilCompletudeService::estVisible)
                .map(citoyen -> matching(citoyen, besoinsCompetences))
                .sorted(Comparator.comparingInt(TalentMatchingResponse::score).reversed())
                .toList();
    }

    private boolean matches(Citoyen citoyen, TalentFiltres f) {
        List<CitoyenCompetence> competences = citoyenCompetenceRepository.findByCitoyenId(citoyen.getId());
        List<String> nomsCompetences = competences.stream()
                .map(item -> item.getCompetence().getNom()).toList();
        List<String> nomsMetiers = competences.stream()
                .map(item -> item.getCompetence().getMetier().getNom()).toList();
        List<String> lieux = localisationValues(citoyen);

        if (!containsAny(f.competence(), nomsCompetences)
                || !containsAny(f.metier(), nomsMetiers)
                || !containsAny(f.localisation(), lieux)) {
            return false;
        }
        if (f.disponibilite() != null && !f.disponibilite().isBlank()
                && !citoyen.getDisponibilite().name().equalsIgnoreCase(f.disponibilite().trim())) {
            return false;
        }
        if (f.q() != null && !f.q().isBlank()) {
            List<String> texte = new ArrayList<>(nomsCompetences);
            texte.addAll(nomsMetiers);
            texte.addAll(lieux);
            if (!containsAny(f.q(), texte)) {
                return false;
            }
        }
        if (f.competenceId() != null && competences.stream().noneMatch(item ->
                item.getCompetence().getId().equals(f.competenceId()) && niveauSuffisant(item, f))) {
            return false;
        }
        if (f.metierId() != null && competences.stream().noneMatch(item ->
                item.getCompetence().getMetier().getId().equals(f.metierId()) && niveauSuffisant(item, f))) {
            return false;
        }
        if (f.competenceId() == null && f.metierId() == null && f.niveauMin() != null
                && competences.stream().noneMatch(item -> niveauSuffisant(item, f))) {
            return false;
        }
        Commune commune = citoyen.getCommune();
        if (f.regionId() != null
                && (commune == null || !commune.getRegion().getId().equals(f.regionId()))) {
            return false;
        }
        return f.communeId() == null || (commune != null && commune.getId().equals(f.communeId()));
    }

    private boolean niveauSuffisant(CitoyenCompetence item, TalentFiltres f) {
        return f.niveauMin() == null || level(item.getNiveau()) >= level(f.niveauMin());
    }

    private List<String> localisationValues(Citoyen citoyen) {
        if (citoyen.getCommune() == null) {
            return List.of();
        }
        return List.of(
                citoyen.getCommune().getNom(),
                citoyen.getCommune().getRegion().getNom()
        );
    }

    private boolean containsAny(String query, List<String> values) {
        return query == null || query.isBlank()
                || values.stream().anyMatch(value -> value.toLowerCase(Locale.ROOT)
                        .contains(query.trim().toLowerCase(Locale.ROOT)));
    }

    private TalentMatchingResponse matching(Citoyen citoyen, List<BesoinCompetence> required) {
        List<CitoyenCompetence> declared =
                citoyenCompetenceRepository.findByCitoyenId(citoyen.getId());
        List<String> matched = required.stream()
                .filter(need -> declared.stream().anyMatch(skill ->
                        skill.getCompetence().getId().equals(need.getCompetence().getId())
                                && level(skill.getNiveau()) >= level(need.getNiveauMinimum())))
                .map(need -> need.getCompetence().getNom())
                .distinct()
                .toList();
        List<String> missing = required.stream()
                .map(item -> item.getCompetence().getNom())
                .filter(name -> !matched.contains(name))
                .distinct()
                .toList();
        int score = Math.round((float) matched.size() * 100 / required.size());
        String grade = score >= 80 ? "EXCELLENT" : score >= 50 ? "BON" : "MOYEN";
        return new TalentMatchingResponse(
                citoyen.getId(),
                toResponse(citoyen),
                score,
                matched.size(),
                grade,
                matched,
                missing,
                matched.size() + " compétence(s) sur " + required.size()
                        + " correspondent au niveau minimum demandé."
        );
    }

    private int level(NiveauCompetence niveau) {
        return switch (niveau) {
            case DEBUTANT -> 1;
            case INTERMEDIAIRE -> 2;
            case EXPERT -> 3;
        };
    }

        private TalentRechercheResponse toResponse(Citoyen citoyen) {
        return toResponse(citoyen, java.util.Set.of());
    }

    private TalentRechercheResponse toResponse(Citoyen citoyen, java.util.Set<UUID> contacts) {
        List<CitoyenCompetence> links =
                citoyenCompetenceRepository.findByCitoyenId(citoyen.getId());
        List<TalentCompetenceResponse> skills = links.stream()
                .map(link -> new TalentCompetenceResponse(
                        link.getCompetence().getId(),
                        link.getCompetence().getNom(),
                        link.getCompetence().getId(),
                        link.getNiveau(),
                        link.getEtat() == EtatCompetence.VALIDEE,
                        !link.getPreuves().isEmpty(),
                        link.getParcours()))
                .distinct()
                .toList();
        String metier = links.stream()
                .map(link -> link.getCompetence().getMetier().getNom())
                .findFirst().orElse(null);
        String secteur = links.stream()
                .map(link -> link.getCompetence().getMetier().getSecteurActivite().getNom())
                .findFirst().orElse(null);
        List<UUID> metierIds = links.stream()
                .map(link -> link.getCompetence().getMetier().getId())
                .distinct()
                .toList();
        int nbValidations = (int) links.stream()
                .filter(link -> link.getEtat() == EtatCompetence.VALIDEE).count();
        int nbPreuves = links.stream().mapToInt(link -> link.getPreuves().size()).sum();
        boolean aPortfolio = !portfolioRealisationRepository.findByCitoyenId(citoyen.getId()).isEmpty();
        Commune commune = citoyen.getCommune();
        return new TalentRechercheResponse(
                citoyen.getId(),
                "Profil-" + citoyen.getId().toString().substring(0, 8),
                metier,
                secteur,
                commune == null ? null : commune.getNom(),
                citoyen.getDisponibilite(),
                citoyen.getDisponibilite() == odk.malicompetences.backend.citoyen.Disponibilite.DISPONIBLE,
                skills,
                commune == null ? null : commune.getRegion().getId(),
                commune == null ? null : commune.getRegion().getNom(),
                commune == null ? null : commune.getId(),
                commune == null ? null : commune.getNom(),
                metierIds,
                nbValidations,
                                nbPreuves,
                aPortfolio,
                contacts.contains(citoyen.getId()),
                contacts.contains(citoyen.getId()) ? citoyen.getPrenom() + " " + citoyen.getNom() : null,
                contacts.contains(citoyen.getId()) ? citoyen.getPhotoUrl() : null
        );
    }
}
