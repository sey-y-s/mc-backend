package odk.malicompetences.backend.competence;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MetierService {
    private final MetierRepository repository;
    private final MetierMapper mapper;

    public MetierService(MetierRepository repository, MetierMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<MetierResponse> lister(UUID secteurId, Boolean actif, String q) {
        String recherche = q == null || q.isBlank() ? null : q.trim();
        return repository.rechercher(secteurId, actif, recherche).stream().map(mapper::toResponse).toList();
    }

    public MetierResponse trouverParId(UUID id) {
        return repository.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Métier introuvable : " + id));
    }

    public MetierResponse trouverParCode(String code) {
        return repository.findByCode(code).map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Métier introuvable pour le code : " + code));
    }
}
