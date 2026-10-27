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

    private odk.malicompetences.backend.territoire.RegionRepository regionRepository;
    private odk.malicompetences.backend.territoire.CommuneRepository communeRepository;
    private CandidatureOpportuniteRepository candidatureRepository;

    /** Référentiels territoriaux et candidatures (injectés par Spring ; absents dans les tests unitaires). */
    @org.springframework.beans.factory.annotation.Autowired
    void setDependances(odk.malicompetences.backend.territoire.RegionRepository regionRepository,
                        odk.malicompetences.backend.territoire.CommuneRepository communeRepository,
                        CandidatureOpportuniteRepository candidatureRepository) {
        this.regionRepository = regionRepository;
        this.communeRepository = communeRepository;
        this.candidatureRepository = candidatureRepository;
    }

    /** Localité de l'opportunité : la commune impose sa région ; aucune des deux = tout le Mali. */
    private void appliquerLocalite(Opportunite op, OpportuniteRequest request) {
        if (regionRepository == null || communeRepository == null) return;
        odk.malicompetences.backend.territoire.Commune commune = request.communeId() == null ? null
                : communeRepository.findById(request.communeId())
                .orElseThrow(() -> new ResourceNotFoundException("Commune introuvable."));
        odk.malicompetences.backend.territoire.Region region = commune != null ? commune.getRegion()
                : request.regionId() == null ? null
                : regionRepository.findById(request.regionId())
                .orElseThrow(() -> new ResourceNotFoundException("Région introuvable."));
        if (commune != null && request.regionId() != null && region != null && !region.getId().equals(request.regionId())) {
            throw new IllegalArgumentException("La commune choisie n'appartient pas à la région indiquée.");
        }
        op.setRegion(region);
        op.setCommune(commune);
    }

    public OpportuniteResponse creerOpportunite(OpportuniteRequest request) {
        CategorieOpportunite categorie = categorieRepository.findById(request.categorieId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Catégorie non trouvée avec l'id : " + request.categorieId()));

        Opportunite opportunite = mapper.toEntity(request, categorie);
        appliquerLocalite(opportunite, request);
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
        appliquerLocalite(op, request);
        return mapper.toResponse(opportuniteRepository.save(op));
    }

    public void supprimerOpportunite(UUID id) {
        if (!opportuniteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Opportunité non trouvée avec l'id : " + id);
        }
        if (candidatureRepository != null) {
            candidatureRepository.deleteAll(candidatureRepository.findByOpportuniteIdOrderByLocaleDescDateCandidatureAsc(id));
        }
        opportuniteRepository.deleteById(id);
    }
}
