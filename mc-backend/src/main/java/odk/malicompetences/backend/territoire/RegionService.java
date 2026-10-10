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

    public RegionService(RegionRepository regionRepository, RegionMapper regionMapper) {
        this.regionRepository = regionRepository;
        this.regionMapper = regionMapper;
    }

    public RegionResponse creerRegion(RegionRequest request) {
        Region region = regionMapper.toEntity(request);
        return regionMapper.toResponse(regionRepository.save(region));
    }

    @Transactional(readOnly = true)
    public RegionResponse trouverParId(UUID id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id : " + id));
        return regionMapper.toResponse(region);
    }

    @Transactional(readOnly = true)
    public List<RegionResponse> listerToutes() {
        return regionRepository.findAll().stream()
                .map(regionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public RegionResponse modifierRegion(UUID id, RegionRequest request) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id : " + id));

        regionMapper.updateEntity(request, region);
        return regionMapper.toResponse(regionRepository.save(region));
    }

    public void supprimerRegion(UUID id) {
        if (!regionRepository.existsById(id)) {
            throw new RuntimeException("Région non trouvée avec l'id : " + id);
        }
        regionRepository.deleteById(id);
    }
}