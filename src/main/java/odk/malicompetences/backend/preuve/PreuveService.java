package odk.malicompetences.backend.preuve;

import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PreuveService {

    private final PreuveRepository preuveRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final PreuveMapper preuveMapper;

    public PreuveService(
            PreuveRepository preuveRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            PreuveMapper preuveMapper
    ) {
        this.preuveRepository = preuveRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.preuveMapper = preuveMapper;
    }

    public PreuveResponse creerPreuve(PreuveRequest request) {
        CitoyenCompetence citoyenCompetence = citoyenCompetenceRepository.findById(request.citoyenCompetenceId())
                .orElseThrow(() -> new RuntimeException("Compétence du citoyen non trouvée avec l'id : " + request.citoyenCompetenceId()));

        Preuve preuve = preuveMapper.toEntity(request, citoyenCompetence);
        Preuve saved = preuveRepository.save(preuve);
        return preuveMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PreuveResponse trouverParId(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preuve non trouvée avec l'id : " + id));
        return preuveMapper.toResponse(preuve);
    }

    @Transactional(readOnly = true)
    public List<PreuveResponse> listerParCitoyenCompetence(UUID citoyenCompetenceId) {
        return preuveRepository.findAll().stream()
                .filter(p -> p.getCitoyenCompetence().getId().equals(citoyenCompetenceId))
                .map(preuveMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PreuveResponse modifierPreuve(UUID id, PreuveRequest request) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preuve non trouvée avec l'id : " + id));

        preuveMapper.updateEntity(request, preuve);
        Preuve updated = preuveRepository.save(preuve);
        return preuveMapper.toResponse(updated);
    }

    public void supprimerPreuve(UUID id) {
        if (!preuveRepository.existsById(id)) {
            throw new RuntimeException("Preuve non trouvée avec l'id : " + id);
        }
        preuveRepository.deleteById(id);
    }
}