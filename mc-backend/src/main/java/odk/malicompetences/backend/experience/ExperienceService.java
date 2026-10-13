package odk.malicompetences.backend.experience;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final CitoyenRepository citoyenRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final ExperienceMapper experienceMapper;

    public ExperienceService(
            ExperienceRepository experienceRepository,
            CitoyenRepository citoyenRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            ExperienceMapper experienceMapper
    ) {
        this.experienceRepository = experienceRepository;
        this.citoyenRepository = citoyenRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.experienceMapper = experienceMapper;
    }

    public ExperienceResponse creerExperience(ExperienceRequest request) {
        verifierProprietaire(request.citoyenId(), "CITOYEN");
        Citoyen citoyen = citoyenRepository.findById(request.citoyenId())
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé avec l'id : " + request.citoyenId()));

        Set<CitoyenCompetence> competences = new HashSet<>();
        if (request.citoyenCompetenceIds() != null && !request.citoyenCompetenceIds().isEmpty()) {
            competences = new HashSet<>(citoyenCompetenceRepository.findAllById(request.citoyenCompetenceIds()));
            if (competences.size() != request.citoyenCompetenceIds().size()
                    || competences.stream().anyMatch(
                            competence -> !competence.getCitoyen().getId().equals(request.citoyenId())
                    )) {
                throw new ResourceNotFoundException(
                        "Une ou plusieurs compétences du citoyen sont introuvables"
                );
            }
        }

        Experience experience = experienceMapper.toEntity(request, citoyen, competences);
        Experience saved = experienceRepository.save(experience);
        return experienceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExperienceResponse trouverParId(UUID id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expérience non trouvée avec l'id : " + id));
        verifierProprietaire(experience.getCitoyen().getId(), "CITOYEN");
        return experienceMapper.toResponse(experience);
    }

    @Transactional(readOnly = true)
    public List<ExperienceResponse> listerToutes() {
        return experienceRepository.findAll().stream()
                .map(experienceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExperienceResponse> listerParCitoyen(UUID citoyenId) {
        verifierProprietaire(citoyenId, "CITOYEN");
        return experienceRepository.findByCitoyenId(citoyenId).stream()
                .map(experienceMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ExperienceResponse modifierExperience(UUID id, ExperienceRequest request) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expérience non trouvée avec l'id : " + id));
        verifierProprietaire(experience.getCitoyen().getId(), "CITOYEN");

        Citoyen citoyen = null;
        if (request.citoyenId() != null) {
            citoyen = citoyenRepository.findById(request.citoyenId())
                    .orElseThrow(() -> new RuntimeException("Citoyen non trouvé avec l'id : " + request.citoyenId()));
        }

        Set<CitoyenCompetence> competences = null;
        if (request.citoyenCompetenceIds() != null) {
            competences = new HashSet<>(citoyenCompetenceRepository.findAllById(request.citoyenCompetenceIds()));
        }

        experienceMapper.updateEntity(request, experience, citoyen, competences);
        Experience updated = experienceRepository.save(experience);
        return experienceMapper.toResponse(updated);
    }

    public void supprimerExperience(UUID id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expérience non trouvée avec l'id : " + id));
        verifierProprietaire(experience.getCitoyen().getId(), "CITOYEN");
        experienceRepository.delete(experience);
    }

    public ExperienceResponse getById(UUID id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expérience introuvable"));

        verifierProprietaire(experience.getCitoyen().getId(), "CITOYEN");
        return experienceMapper.toResponse(experience);
    }

    public ExperienceResponse modifier(UUID id, ExperienceRequest request) {
        return modifier(id, request.citoyenId(), request);
    }

    public ExperienceResponse modifier(UUID id, UUID citoyenId, ExperienceRequest request) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expérience introuvable"));
        verifierProprietaire(experience.getCitoyen().getId(), "CITOYEN");
        if (!experience.getCitoyen().getId().equals(citoyenId)
                || (request.citoyenId() != null
                && !request.citoyenId().equals(experience.getCitoyen().getId()))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Une expérience ne peut pas être transférée vers un autre citoyen"
            );
        }

        experience.setTitre(request.titre());
        experience.setDescription(request.description());
        experience.setEntreprise(request.entreprise());
        experience.setDateDebut(request.dateDebut());
        experience.setDateFin(request.dateFin());
        experience.setEnCours(request.enCours());

        experience.getCompetences().clear();

        if (request.citoyenCompetenceIds() != null) {
            Set<CitoyenCompetence> competences =
                    new HashSet<>(citoyenCompetenceRepository
                            .findAllById(request.citoyenCompetenceIds()));

            if (competences.size() != request.citoyenCompetenceIds().size()
                    || competences.stream().anyMatch(
                            competence -> !competence.getCitoyen().getId()
                                    .equals(experience.getCitoyen().getId())
                    )) {
                throw new ResourceNotFoundException(
                        "Une ou plusieurs compétences du citoyen sont introuvables");
            }

            experience.getCompetences().addAll(competences);
        }

        return experienceMapper.toResponse(
                experienceRepository.save(experience)
        );
    }

    @Transactional(readOnly = true)
    public ExperienceResponse trouverPourCitoyen(UUID id, UUID citoyenId) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expérience introuvable"));
        if (!experience.getCitoyen().getId().equals(citoyenId)) {
            throw new ResourceNotFoundException("Expérience introuvable");
        }
        verifierProprietaire(citoyenId, "CITOYEN");
        return experienceMapper.toResponse(experience);
    }

    public void supprimer(UUID id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expérience introuvable"));
        verifierProprietaire(experience.getCitoyen().getId(), "CITOYEN");

        experienceRepository.delete(experience);
    }

    public void supprimer(UUID id, UUID citoyenId) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expérience introuvable"));
        if (!experience.getCitoyen().getId().equals(citoyenId)) {
            throw new ResourceNotFoundException("Expérience introuvable");
        }
        verifierProprietaire(citoyenId, "CITOYEN");
        experienceRepository.delete(experience);
    }


    /** Vérifie directement que l'utilisateur connecté possède la ressource. */
    private void verifierProprietaire(UUID proprietaireId, String roleAttendu) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean administrateur = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (administrateur) return;
        boolean bonRole = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleAttendu));
        boolean bonUtilisateur = false;
        if (authentication != null && authentication.getName() != null && proprietaireId != null) {
            try { bonUtilisateur = proprietaireId.equals(UUID.fromString(authentication.getName())); }
            catch (IllegalArgumentException ignored) { bonUtilisateur = false; }
        }
        if (!bonRole || !bonUtilisateur) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé : cette ressource ne vous appartient pas.");
        }
    }

    private boolean estAdministrateur() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}