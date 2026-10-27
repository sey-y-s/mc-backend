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
                .orElseThrow(() -> new IllegalArgumentException("Région introuvable."));
        Region[] rattachement = hierarchie.rattachementCommune(region, cercle(request.cercleId()));

        Commune commune = communeMapper.toEntity(request, rattachement[0]);
        commune.setCercle(rattachement[1]);
        return communeMapper.toResponse(communeRepository.save(commune));
    }

    @Transactional(readOnly = true)
    public CommuneResponse trouverParId(UUID id) {
        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Commune non trouvée avec l'id : " + id));
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
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Commune non trouvée avec l'id : " + id));

        Region region = null;
        if (request.regionId() != null) {
            region = regionRepository.findById(request.regionId())
                    .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Région non trouvée avec l'id : " + request.regionId()));
        }

        Region[] rattachement = hierarchie.rattachementCommune(
                region != null ? region : commune.getRegion(),
                request.cercleId() != null ? cercle(request.cercleId()) : (region == null ? commune.getCercle() : null));
        communeMapper.updateEntity(request, commune, rattachement[0]);
        commune.setCercle(rattachement[1]);
        return communeMapper.toResponse(communeRepository.save(commune));
    }

    private Region cercle(UUID cercleId) {
        if (cercleId == null) return null;
        return regionRepository.findById(cercleId)
                .orElseThrow(() -> new IllegalArgumentException("Cercle introuvable."));
    }

    private HierarchieTerritoriale hierarchie;

    @org.springframework.beans.factory.annotation.Autowired
    void setHierarchie(HierarchieTerritoriale hierarchie) {
        this.hierarchie = hierarchie;
    }

    public void supprimerCommune(UUID id) {
        if (!communeRepository.existsById(id)) {
            throw new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Commune non trouvée avec l'id : " + id);
        }
        communeRepository.deleteById(id);
    }
}