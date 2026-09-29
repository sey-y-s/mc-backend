package odk.malicompetences.backend.territoire;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommuneService {

    private final CommuneRepository communeRepository;
    private final RegionRepository regionRepository;
    private final CommuneMapper communeMapper;

    public CommuneService(
            CommuneRepository communeRepository,
            RegionRepository regionRepository,
            CommuneMapper communeMapper
    ) {
        this.communeRepository = communeRepository;
        this.regionRepository = regionRepository;
        this.communeMapper = communeMapper;
    }

    public CommuneResponse creerCommune(CommuneRequest request) {
        Region region = regionRepository.findById(request.regionId())
                .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id : " + request.regionId()));

        Commune commune = communeMapper.toEntity(request, region);
        return communeMapper.toResponse(communeRepository.save(commune));
    }

    @Transactional(readOnly = true)
    public CommuneResponse trouverParId(UUID id) {
        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commune non trouvée avec l'id : " + id));
        return communeMapper.toResponse(commune);
    }

    @Transactional(readOnly = true)
    public List<CommuneResponse> listerToutes() {
        return communeRepository.findAll().stream()
                .map(communeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommuneResponse> listerParRegion(UUID regionId) {
        return communeRepository.findAll().stream()
                .filter(c -> c.getRegion().getId().equals(regionId))
                .map(communeMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CommuneResponse modifierCommune(UUID id, CommuneRequest request) {
        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commune non trouvée avec l'id : " + id));

        Region region = null;
        if (request.regionId() != null) {
            region = regionRepository.findById(request.regionId())
                    .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id : " + request.regionId()));
        }

        communeMapper.updateEntity(request, commune, region);
        return communeMapper.toResponse(communeRepository.save(commune));
    }

    public void supprimerCommune(UUID id) {
        if (!communeRepository.existsById(id)) {
            throw new RuntimeException("Commune non trouvée avec l'id : " + id);
        }
        communeRepository.deleteById(id);
    }
}