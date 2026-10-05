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
        Citoyen citoyen = citoyenRepository.findById(request.citoyenId())
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé avec l'id : " + request.citoyenId()));

        Set<CitoyenCompetence> competences = new HashSet<>();
        if (request.citoyenCompetenceIds() != null && !request.citoyenCompetenceIds().isEmpty()) {
            competences = new HashSet<>(citoyenCompetenceRepository.findAllById(request.citoyenCompetenceIds()));
        }

        Experience experience = experienceMapper.toEntity(request, citoyen, competences);
        Experience saved = experienceRepository.save(experience);
        return experienceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExperienceResponse trouverParId(UUID id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expérience non trouvée avec l'id : " + id));
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
        return experienceRepository.findAll().stream()
                .filter(exp -> exp.getCitoyen().getId().equals(citoyenId))
                .map(experienceMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ExperienceResponse modifierExperience(UUID id, ExperienceRequest request) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expérience non trouvée avec l'id : " + id));

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
        if (!experienceRepository.existsById(id)) {
            throw new RuntimeException("Expérience non trouvée avec l'id : " + id);
        }
        experienceRepository.deleteById(id);
    }

    public ExperienceResponse getById(UUID id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expérience introuvable"));

        return experienceMapper.toResponse(experience);
    }

    public ExperienceResponse modifier(UUID id, ExperienceRequest request) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expérience introuvable"));

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

            if (competences.size() != request.citoyenCompetenceIds().size()) {
                throw new ResourceNotFoundException(
                        "Une ou plusieurs compétences du citoyen sont introuvables");
            }

            experience.getCompetences().addAll(competences);
        }

        return experienceMapper.toResponse(
                experienceRepository.save(experience)
        );
    }

    public void supprimer(UUID id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expérience introuvable"));

        experienceRepository.delete(experience);
    }

}