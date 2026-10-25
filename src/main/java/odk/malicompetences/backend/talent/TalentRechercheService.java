package odk.malicompetences.backend.talent;


import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.besoin.BesoinCompetence;
import odk.malicompetences.backend.besoin.BesoinCompetenceRepository;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.NiveauCompetence;
import odk.malicompetences.backend.common.statut.TypePreuveCitoyen;
import odk.malicompetences.backend.common.statut.StatutValidationCompetence;
import java.time.LocalDate;
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

    public TalentRechercheService(
            CitoyenRepository citoyenRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            BesoinRepository besoinRepository,
            BesoinCompetenceRepository besoinCompetenceRepository
    ) {
        this.citoyenRepository = citoyenRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.besoinRepository = besoinRepository;
        this.besoinCompetenceRepository = besoinCompetenceRepository;
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
        List<Citoyen> trouves = citoyenRepository.findAll().stream()
                .filter(Citoyen::isProfilPublic)
                .filter(citoyen -> matches(citoyen, filtres))
                .sorted(Comparator.comparing(citoyen -> citoyen.getId().toString()))
                .toList();
        if (size != null && size > 0) {
            int debut = Math.min(Math.max(page == null ? 0 : page, 0) * size, trouves.size());
            int fin = Math.min(debut + size, trouves.size());
            trouves = trouves.subList(debut, fin);
        }
        return trouves.stream().map(this::toResponse).toList();
    }

    public TalentRechercheResponse trouverParId(UUID id) {
        Citoyen citoyen = citoyenRepository.findByIdentifiantPublic(id)
                .filter(Citoyen::isProfilPublic)
                .orElseThrow(() -> new ResourceNotFoundException("Profil talent introuvable."));
        return toResponse(citoyen);
    }

    public List<TalentMatchingResponse> correspondances(UUID besoinId, UUID organisationId, boolean administrateur) {
        Besoin besoin = besoinRepository.findById(besoinId)
                .orElseThrow(() -> new ResourceNotFoundException("Besoin introuvable."));
        if (!administrateur && !besoin.getOrganisation().getId().equals(organisationId)) {
            throw new AccessDeniedException("Accès refusé à ce besoin.");
        }
        if (besoin.getStatut() != odk.malicompetences.backend.besoin.StatutBesoin.OUVERT
                || (besoin.getDateLimite() != null && besoin.getDateLimite().isBefore(LocalDate.now()))) {
            throw new AccessDeniedException("Le matching est disponible uniquement pour les besoins ouverts et non expirés.");
        }
        List<BesoinCompetence> besoinsCompetences = besoinCompetenceRepository.findByBesoin_Id(besoinId);
        if (besoinsCompetences == null || besoinsCompetences.isEmpty()) {
            return List.of();
        }

        return citoyenRepository.findAll().stream()
                .filter(Citoyen::isProfilPublic)
                .map(citoyen -> matching(citoyen, besoinsCompetences))
                .filter(resultat -> resultat.score() > 0)
                .sorted(Comparator.comparingInt(TalentMatchingResponse::score).reversed())
                .toList();
    }

    private boolean matches(Citoyen citoyen, TalentFiltres f) {
        List<CitoyenCompetence> competences = citoyenCompetenceRepository.findByCitoyenId(citoyen.getId());
        List<String> nomsCompetences = competences.stream()
                .map(item -> item.getCompetence().getNom()).toList();
        List<String> nomsMetiers = competences.stream()
                .flatMap(item -> item.getCompetence().getMetiers().stream())
                .map(odk.malicompetences.backend.competence.Metier::getNom).distinct().toList();
        List<String> lieux = localisationValues(citoyen);

        if (!containsAny(f.competence(), nomsCompetences)
                || !containsAny(f.metier(), nomsMetiers)
                || !containsAny(f.localisation(), lieux)) {
            return false;
        }
        if (f.disponibilite() != null && !f.disponibilite().isBlank()) {
            String filtreDisponibilite = f.disponibilite().trim().toLowerCase(Locale.ROOT);
            if (!filtreDisponibilite.equals("true") && !filtreDisponibilite.equals("false")) return false;
            if (citoyen.isDisponible() != Boolean.parseBoolean(filtreDisponibilite)) return false;
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
                item.getCompetence().getMetiers().stream().anyMatch(m -> m.getId().equals(f.metierId())) && niveauSuffisant(item, f))) {
            return false;
        }
        if (f.competenceId() == null && f.metierId() == null && f.niveauMin() != null
                && competences.stream().noneMatch(item -> niveauSuffisant(item, f))) {
            return false;
        }
        Commune commune = citoyen.getCommune();
        if (f.regionId() != null
                && (commune == null || !commune.getTerritoire().getId().equals(f.regionId()))) {
            return false;
        }
        return f.communeId() == null || (commune != null && commune.getId().equals(f.communeId()));
    }

    private boolean niveauSuffisant(CitoyenCompetence item, TalentFiltres f) {
        return f.niveauMin() == null || (item.getNiveauDeclare() != null && level(item.getNiveauDeclare()) >= level(f.niveauMin()));
    }

    private List<String> localisationValues(Citoyen citoyen) {
        if (citoyen.getCommune() == null) {
            return List.of();
        }
        return List.of(
                citoyen.getCommune().getNom(),
                citoyen.getCommune().getTerritoire().getNom()
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
                                && niveauValidePourOffre(skill)
                                && (need.getNiveauRequis() == null
                                    || level(skill.getNiveauValide()) >= level(need.getNiveauRequis()))))
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
                citoyen.getIdentifiantPublic(),
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

    private boolean niveauValidePourOffre(CitoyenCompetence competence) {
        return competence.getStatutValidation() == StatutValidationCompetence.VALIDEE
                && competence.getNiveauValide() != null
                && competence.getValideeLe() != null
                && (competence.getValideJusquAu() == null || !competence.getValideJusquAu().isBefore(LocalDate.now()));
    }

    private TalentRechercheResponse toResponse(Citoyen citoyen) {
        List<CitoyenCompetence> links =
                citoyenCompetenceRepository.findByCitoyenId(citoyen.getId());
        List<TalentCompetenceResponse> skills = links.stream()
                .map(link -> new TalentCompetenceResponse(
                        link.getCompetence().getId(),
                        link.getCompetence().getNom(),
                        link.getCompetence().getId(),
                        niveauValidePourOffre(link) ? link.getNiveauValide() : link.getNiveauDeclare(),
                        niveauValidePourOffre(link),
                        link.getPreuves().stream().anyMatch(preuve -> preuve.isVisibleProfil())))
                .distinct()
                .toList();
        String metier = links.stream()
                .flatMap(link -> link.getCompetence().getMetiers().stream())
                .sorted(Comparator.comparing(odk.malicompetences.backend.competence.Metier::getNom))
                .map(odk.malicompetences.backend.competence.Metier::getNom)
                .findFirst().orElse(null);
        String secteur = links.stream()
                .flatMap(link -> link.getCompetence().getMetiers().stream())
                .sorted(Comparator.comparing(odk.malicompetences.backend.competence.Metier::getNom))
                .map(odk.malicompetences.backend.competence.Metier::getSecteurActivite)
                .filter(java.util.Objects::nonNull)
                .map(odk.malicompetences.backend.competence.SecteurActivite::getNom)
                .findFirst().orElse(null);
        List<UUID> metierIds = links.stream().flatMap(link -> link.getCompetence().getMetiers().stream())
                .map(odk.malicompetences.backend.competence.Metier::getId).distinct().toList();
        int nbValidations = (int) links.stream().filter(this::niveauValidePourOffre).count();
        int nbPreuves = (int) links.stream().flatMap(link -> link.getPreuves().stream()).filter(preuve -> preuve.isVisibleProfil()).count();
        boolean aPortfolio = links.stream().flatMap(link -> link.getPreuves().stream())
                .anyMatch(preuve -> preuve.isVisibleProfil() && (preuve.getType() == TypePreuveCitoyen.PORTFOLIO
                        || preuve.getType() == TypePreuveCitoyen.PHOTO || preuve.getType() == TypePreuveCitoyen.VIDEO));
        Commune commune = citoyen.getCommune();
        return new TalentRechercheResponse(
                citoyen.getIdentifiantPublic(),
                "Profil-" + citoyen.getIdentifiantPublic().toString().substring(0, 8),
                metier,
                secteur,
                commune == null ? null : commune.getNom(),
                citoyen.isDisponible(),
                skills,
                commune == null ? null : commune.getTerritoire().getId(),
                commune == null ? null : commune.getTerritoire().getNom(),
                commune == null ? null : commune.getId(),
                commune == null ? null : commune.getNom(),
                metierIds,
                nbValidations,
                nbPreuves,
                aPortfolio
        );
    }
}
