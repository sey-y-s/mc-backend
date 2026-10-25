package odk.malicompetences.backend.opportunite;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategorieOpportuniteService {

    private final CategorieOpportuniteRepository repository;
    private final CategorieOpportuniteMapper mapper;

    public CategorieOpportuniteService(CategorieOpportuniteRepository repository, CategorieOpportuniteMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public CategorieOpportuniteResponse creer(CategorieOpportuniteRequest request) {
        CategorieOpportunite cat = mapper.toEntity(request);
        return mapper.toResponse(repository.save(cat));
    }

    @Transactional(readOnly = true)
    public List<CategorieOpportuniteResponse> listerToutes() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategorieOpportuniteResponse trouverParId(UUID id) {
        CategorieOpportunite cat = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie d'opportunité non trouvée avec l'id : " + id));
        return mapper.toResponse(cat);
    }

    public void supprimer(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Catégorie non trouvée avec l'id : " + id);
        }
        repository.deleteById(id);
    }
}