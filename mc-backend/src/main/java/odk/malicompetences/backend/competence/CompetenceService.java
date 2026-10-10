package odk.malicompetences.backend.competence;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CompetenceService {

    private final CompetenceRepository competenceRepository;
    private final CompetenceMapper competenceMapper;

    public CompetenceService(
            CompetenceRepository competenceRepository,
            CompetenceMapper competenceMapper
    ) {
        this.competenceRepository = competenceRepository;
        this.competenceMapper = competenceMapper;
    }

    @Transactional(readOnly = true)
    public List<CompetenceResponse> trouverToutes() {
        return competenceRepository.findAll()
                .stream()
                .map(competenceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompetenceResponse trouverParId(UUID id) {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Compétence introuvable"
                ));

        return competenceMapper.toResponse(competence);
    }
}