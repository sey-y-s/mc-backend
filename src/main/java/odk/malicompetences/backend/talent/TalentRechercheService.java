package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.besoin.BesoinCompetence;
import odk.malicompetences.backend.besoin.BesoinCompetenceRepository;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.NiveauCompetence;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return citoyenRepository.findAll().stream()
                .filter(citoyen -> matches(citoyen, competence, metier, localisation, disponibilite))
                .map(this::toResponse)
                .toList();
    }

    public TalentRechercheResponse trouverParId(UUID id) {
        Citoyen citoyen = citoyenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profil talent introuvable."));
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
                .map(citoyen -> matching(citoyen, besoinsCompetences))
                .sorted(Comparator.comparingInt(TalentMatchingResponse::score).reversed())
                .toList();
    }

    private boolean matches(
            Citoyen citoyen,
            String competence,
            String metier,
            String localisation,
            String disponibilite
    ) {
        List<CitoyenCompetence> competences = citoyenCompetenceRepository.findByCitoyenId(citoyen.getId());
        return containsAny(competence, competences.stream()
                        .map(item -> item.getCompetence().getNom()).toList())
                && containsAny(metier, competences.stream()
                        .map(item -> item.getCompetence().getMetier().getNom()).toList())
                && containsAny(localisation, localisationValues(citoyen))
                && (disponibilite == null || disponibilite.isBlank()
                        || citoyen.getDisponibilite().name().equalsIgnoreCase(disponibilite.trim()));
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
        List<CitoyenCompetence> links =
                citoyenCompetenceRepository.findByCitoyenId(citoyen.getId());
        List<TalentCompetenceResponse> skills = links.stream()
                .map(link -> new TalentCompetenceResponse(
                        link.getCompetence().getId(),
                        link.getCompetence().getNom()))
                .distinct()
                .toList();
        String metier = links.stream()
                .map(link -> link.getCompetence().getMetier().getNom())
                .findFirst().orElse(null);
        String secteur = links.stream()
                .map(link -> link.getCompetence().getMetier().getSecteurActivite().getNom())
                .findFirst().orElse(null);
        String city = citoyen.getCommune() == null ? null : citoyen.getCommune().getNom();
        return new TalentRechercheResponse(
                citoyen.getId(),
                "Profil-" + citoyen.getId().toString().substring(0, 8),
                metier,
                secteur,
                city,
                citoyen.getDisponibilite(),
                citoyen.getDisponibilite() == odk.malicompetences.backend.citoyen.Disponibilite.DISPONIBLE,
                skills
        );
    }
}
