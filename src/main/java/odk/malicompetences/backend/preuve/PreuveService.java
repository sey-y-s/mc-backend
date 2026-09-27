package odk.malicompetences.backend.preuve;

import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PreuveService {

    private final PreuveRepository preuveRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final PreuveMapper mapper;

    public PreuveService(
            PreuveRepository preuveRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            PreuveMapper mapper
    ) {
        this.preuveRepository = preuveRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.mapper = mapper;
    }

    public PreuveResponse ajouter(
            UUID citoyenCompetenceId,
            PreuveRequest request
    ) {
        CitoyenCompetence citoyenCompetence =
                citoyenCompetenceRepository.findById(
                        citoyenCompetenceId
                ).orElseThrow(() -> new IllegalArgumentException(
                        "Compétence du citoyen introuvable"
                ));

        Preuve preuve = new Preuve();

        preuve.setCitoyenCompetence(citoyenCompetence);
        preuve.setType(request.type());
        preuve.setTitre(request.titre());
        preuve.setDescription(request.description());
        preuve.setFichierUrl(request.fichierUrl());
        preuve.setDateDepot(LocalDateTime.now());

        return mapper.toResponse(
                preuveRepository.save(preuve)
        );
    }

    @Transactional(readOnly = true)
    public List<PreuveResponse> trouverParCompetence(
            UUID citoyenCompetenceId
    ) {
        return preuveRepository
                .findByCitoyenCompetenceId(citoyenCompetenceId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}