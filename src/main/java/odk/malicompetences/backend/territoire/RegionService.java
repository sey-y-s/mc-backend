package odk.malicompetences.backend.territoire;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RegionService {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    private HierarchieTerritoriale hierarchie;

    @org.springframework.beans.factory.annotation.Autowired
    void setHierarchie(HierarchieTerritoriale hierarchie) {
        this.hierarchie = hierarchie;
    }

    public RegionService(RegionRepository regionRepository, RegionMapper regionMapper) {
        this.regionRepository = regionRepository;
        this.regionMapper = regionMapper;
    }

    public RegionResponse creerRegion(RegionRequest request) {
        Region region = regionMapper.toEntity(request);
        Region parent = parent(request.parentId());
        hierarchie.verifierTerritoire(region.getNiveau(), parent);
        region.setParent(parent);
        long count = regionRepository.countByNiveau(region.getNiveau());
        long maximum = region.getNiveau() == NiveauTerritoire.DISTRICT ? 1
                : region.getNiveau() == NiveauTerritoire.CERCLE ? Long.MAX_VALUE : 19;
        if (count >= maximum) {
            throw new IllegalStateException(region.getNiveau() == NiveauTerritoire.DISTRICT
                    ? "Le référentiel ne peut contenir qu'un seul district."
                    : "Le référentiel national contient déjà ses 19 régions.");
        }
        return regionMapper.toResponse(regionRepository.save(region));
    }

    @Transactional(readOnly = true)
    public RegionResponse trouverParId(UUID id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Région non trouvée avec l'id : " + id));
        return regionMapper.toResponse(region);
    }

    @Transactional(readOnly = true)
    public List<RegionResponse> listerToutes() {
        // Régions et district uniquement : les cercles se listent par région (listerCercles).
        return regionRepository.findAllByNiveauNot(NiveauTerritoire.CERCLE).stream()
                .map(regionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public RegionResponse modifierRegion(UUID id, RegionRequest request) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Région non trouvée avec l'id : " + id));

        if (region.getCodeStable() != null && request.codeStable() != null
                && !region.getCodeStable().equals(request.codeStable())) {
            throw new IllegalStateException("Le code territorial stable est immuable une fois attribué.");
        }
        if (request.codeStable() != null && regionRepository.findByCodeStable(request.codeStable())
                .filter(existing -> !existing.getId().equals(id)).isPresent()) {
            throw new IllegalStateException("Ce code territorial est déjà utilisé.");
        }
        if (request.niveau() != null && region.getNiveau() != null && !region.getNiveau().equals(request.niveau())) {
            throw new IllegalStateException("Le niveau territorial est immuable une fois attribué.");
        }
        Region parent = request.parentId() != null ? parent(request.parentId()) : region.getParent();
        hierarchie.verifierTerritoire(region.getNiveau(), parent);
        region.setParent(parent);
        regionMapper.updateEntity(request, region);
        return regionMapper.toResponse(regionRepository.save(region));
    }

    @Transactional(readOnly = true)
    public List<RegionResponse> listerCercles(UUID regionId) {
        return regionRepository.findAllByParent_IdAndNiveau(regionId, NiveauTerritoire.CERCLE).stream()
                .sorted(java.util.Comparator.comparing(Region::getNom))
                .map(regionMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Region parent(UUID parentId) {
        if (parentId == null) return null;
        return regionRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Territoire parent introuvable."));
    }

    public void supprimerRegion(UUID id) {
        if (!regionRepository.existsById(id)) {
            throw new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Région non trouvée avec l'id : " + id);
        }
        regionRepository.deleteById(id);
    }
}