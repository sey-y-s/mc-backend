package odk.malicompetences.backend.opportunite;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OpportuniteService {

    private final OpportuniteRepository opportuniteRepository;
    private final CategorieOpportuniteRepository categorieRepository;
    private final OpportuniteMapper mapper;

    public OpportuniteService(
            OpportuniteRepository opportuniteRepository,
            CategorieOpportuniteRepository categorieRepository,
            OpportuniteMapper mapper
    ) {
        this.opportuniteRepository = opportuniteRepository;
        this.categorieRepository = categorieRepository;
        this.mapper = mapper;
    }

    public OpportuniteResponse creerOpportunite(OpportuniteRequest request) {
        CategorieOpportunite categorie = categorieRepository.findById(request.categorieId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'id : " + request.categorieId()));

        Opportunite opportunite = mapper.toEntity(request, categorie);
        return mapper.toResponse(opportuniteRepository.save(opportunite));
    }

    @Transactional(readOnly = true)
    public OpportuniteResponse trouverParId(UUID id) {
        Opportunite op = opportuniteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée avec l'id : " + id));
        return mapper.toResponse(op);
    }

    @Transactional(readOnly = true)
    public List<OpportuniteResponse> listerToutes() {
        return opportuniteRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public OpportuniteResponse modifierOpportunite(UUID id, OpportuniteRequest request) {
        Opportunite op = opportuniteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée avec l'id : " + id));

        CategorieOpportunite categorie = null;
        if (request.categorieId() != null) {
            categorie = categorieRepository.findById(request.categorieId())
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'id : " + request.categorieId()));
        }

        mapper.updateEntity(request, op, categorie);
        return mapper.toResponse(opportuniteRepository.save(op));
    }

    public void supprimerOpportunite(UUID id) {
        if (!opportuniteRepository.existsById(id)) {
            throw new RuntimeException("Opportunité non trouvée avec l'id : " + id);
        }
        opportuniteRepository.deleteById(id);
    }
}