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

    public ExperienceResponse ajouter(
            UUID citoyenId,
            ExperienceRequest request
    ) {
        Citoyen citoyen = citoyenRepository.findById(citoyenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Citoyen introuvable"
                ));

        Experience experience = new Experience();

        experience.setCitoyen(citoyen);
        experience.setTitre(request.titre());
        experience.setDescription(request.description());
        experience.setEntreprise(request.entreprise());
        experience.setDateDebut(request.dateDebut());
        experience.setDateFin(request.dateFin());
        experience.setEnCours(
                request.enCours() != null && request.enCours()
        );

        if (request.citoyenCompetenceIds() != null
                && !request.citoyenCompetenceIds().isEmpty()) {

            List<CitoyenCompetence> competences =
                    citoyenCompetenceRepository.findAllById(
                            request.citoyenCompetenceIds()
                    );

            if (competences.size()
                    != request.citoyenCompetenceIds().size()) {
                throw new IllegalArgumentException(
                        "Une ou plusieurs compétences sont introuvables"
                );
            }

            experience.setCompetences(new HashSet<>(competences));
        }

        return experienceMapper.toResponse(
                experienceRepository.save(experience)
        );
    }

    @Transactional(readOnly = true)
    public List<ExperienceResponse> trouverParCitoyen(
            UUID citoyenId
    ) {
        return experienceRepository
                .findByCitoyenId(citoyenId)
                .stream()
                .map(experienceMapper::toResponse)
                .toList();
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