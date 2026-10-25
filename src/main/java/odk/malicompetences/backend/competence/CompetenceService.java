package odk.malicompetences.backend.competence;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CompetenceService {
    private final CompetenceRepository repository;
    private final CompetenceMapper mapper;

    public CompetenceService(CompetenceRepository repository, CompetenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CompetenceResponse> trouverToutes(UUID metierId, UUID secteurId, Boolean actif, String q) {
        String recherche = q == null || q.isBlank() ? null : q.trim();
        return repository.rechercher(metierId, secteurId, actif, recherche).stream()
                .map(mapper::toResponse).toList();
    }

    public CompetenceResponse trouverParId(UUID id) {
        return repository.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Compétence introuvable : " + id));
    }

    public CompetenceResponse trouverParCode(String code) {
        return repository.findByCode(code).map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Compétence introuvable pour le code : " + code));
    }
}
