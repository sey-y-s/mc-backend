package odk.malicompetences.backend.opportunite;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Catégorie non trouvée avec l'id : " + request.categorieId()));

        Opportunite opportunite = mapper.toEntity(request, categorie);
        return mapper.toResponse(opportuniteRepository.save(opportunite));
    }

    /** Retourne uniquement les opportunités publiées et non expirées. */
    @Transactional(readOnly = true)
    public List<OpportuniteResponse> listerToutes() {
        return opportuniteRepository.findVisibles(StatutOpportunite.PUBLIEE, LocalDate.now())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** Réservé à l'espace de gestion : inclut brouillons et archives. */
    @Transactional(readOnly = true)
    public List<OpportuniteResponse> listerPourGestion() {
        return opportuniteRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** Un détail public n'est accessible que si l'opportunité est visible. */
    @Transactional(readOnly = true)
    public OpportuniteResponse trouverParId(UUID id) {
        Opportunite op = opportuniteRepository.findVisibleById(
                        id, StatutOpportunite.PUBLIEE, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Opportunité non trouvée."));
        return mapper.toResponse(op);
    }

    @Transactional(readOnly = true)
    public OpportuniteResponse trouverPourGestion(UUID id) {
        Opportunite op = opportuniteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunité non trouvée avec l'id : " + id));
        return mapper.toResponse(op);
    }

    public OpportuniteResponse modifierOpportunite(UUID id, OpportuniteRequest request) {
        Opportunite op = opportuniteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunité non trouvée avec l'id : " + id));

        CategorieOpportunite categorie = null;
        if (request.categorieId() != null) {
            categorie = categorieRepository.findById(request.categorieId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Catégorie non trouvée avec l'id : " + request.categorieId()));
        }

        mapper.updateEntity(request, op, categorie);
        return mapper.toResponse(opportuniteRepository.save(op));
    }

    public void supprimerOpportunite(UUID id) {
        if (!opportuniteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Opportunité non trouvée avec l'id : " + id);
        }
        opportuniteRepository.deleteById(id);
    }
}
