package odk.malicompetences.backend.competence;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CitoyenCompetenceService {

    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final CitoyenRepository citoyenRepository;
    private final CompetenceRepository competenceRepository;
    private final CitoyenCompetenceMapper mapper;

    public CitoyenCompetenceService(
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            CitoyenRepository citoyenRepository,
            CompetenceRepository competenceRepository,
            CitoyenCompetenceMapper mapper
    ) {
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.citoyenRepository = citoyenRepository;
        this.competenceRepository = competenceRepository;
        this.mapper = mapper;
    }

    public CitoyenCompetenceResponse ajouter(
            UUID citoyenId,
            CitoyenCompetenceRequest request
    ) {
        Citoyen citoyen = citoyenRepository.findById(citoyenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Citoyen introuvable"
                ));

        Competence competence = competenceRepository.findById(
                request.competenceId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Compétence introuvable"
        ));

        if (citoyenCompetenceRepository
                .existsByCitoyenIdAndCompetenceId(
                        citoyenId,
                        request.competenceId()
                )) {
            throw new IllegalStateException(
                    "Cette compétence est déjà déclarée"
            );
        }

        CitoyenCompetence citoyenCompetence =
                new CitoyenCompetence();

        citoyenCompetence.setCitoyen(citoyen);
        citoyenCompetence.setCompetence(competence);
        citoyenCompetence.setNiveau(request.niveau());
        citoyenCompetence.setEtat(EtatCompetence.DECLAREE);
        citoyenCompetence.setDateDeclaration(LocalDateTime.now());

        return mapper.toResponse(
                citoyenCompetenceRepository.save(citoyenCompetence)
        );
    }

    @Transactional(readOnly = true)
    public List<CitoyenCompetenceResponse> trouverParCitoyen(
            UUID citoyenId
    ) {
        return citoyenCompetenceRepository
                .findByCitoyenId(citoyenId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}