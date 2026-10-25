package odk.malicompetences.backend.territoire;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.pagination.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommuneService {

    private final CommuneRepository communeRepository;
    private final TerritoireRepository territoireRepository;
    private final CommuneMapper communeMapper;

    public CommuneService(
            CommuneRepository communeRepository,
            TerritoireRepository territoireRepository,
            CommuneMapper communeMapper
    ) {
        this.communeRepository = communeRepository;
        this.territoireRepository = territoireRepository;
        this.communeMapper = communeMapper;
    }

    public CommuneResponse creerCommune(CommuneRequest request) {
        if (communeRepository.existsByCode(request.code())) {
            throw new IllegalStateException("Ce code de commune est déjà utilisé");
        }

        Territoire territoire = territoireRepository.findById(request.territoireId())
                .orElseThrow(() -> new IllegalArgumentException("Territoire de rattachement introuvable"));

        Commune commune = communeMapper.toEntity(request, territoire);
        return communeMapper.toResponse(communeRepository.save(commune));
    }

    @Transactional(readOnly = true)
    public PageResponse<CommuneResponse> lister(
            UUID territoireId,
            Boolean actif,
            String q,
            Pageable pageable
    ) {
        Page<Commune> page = communeRepository.rechercher(territoireId, actif, q, pageable);
        return PageResponse.from(page, communeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CommuneResponse> listerToutes() {
        return communeRepository.findAll().stream()
                .map(communeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommuneResponse> listerParRegion(UUID regionId) {
        return communeRepository.findCommunesRecursive(regionId).stream()
                .map(communeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommuneResponse trouverParId(UUID id) {
        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commune introuvable : " + id));
        return communeMapper.toResponse(commune);
    }

    @Transactional(readOnly = true)
    public CommuneResponse trouverParCode(String code) {
        Commune commune = communeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Commune introuvable pour le code : " + code));
        return communeMapper.toResponse(commune);
    }

    public CommuneResponse modifierCommune(UUID id, CommuneRequest request) {
        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commune introuvable : " + id));

        if (!commune.getCode().equals(request.code()) && communeRepository.existsByCode(request.code())) {
            throw new IllegalStateException("Ce code de commune est déjà utilisé");
        }

        Territoire territoire = territoireRepository.findById(request.territoireId())
                .orElseThrow(() -> new IllegalArgumentException("Territoire de rattachement introuvable"));

        commune.setCode(request.code());
        commune.setNom(request.nom());
        commune.setTerritoire(territoire);
        if (request.actif() != null) {
            commune.setActif(request.actif());
        }

        return communeMapper.toResponse(communeRepository.save(commune));
    }

    public CommuneResponse patchCommune(UUID id, CommuneRequest request) {
        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commune introuvable : " + id));

        if (request.code() != null && !request.code().isBlank() && !request.code().equals(commune.getCode())) {
            if (communeRepository.existsByCode(request.code())) {
                throw new IllegalStateException("Ce code de commune est déjà utilisé");
            }
            commune.setCode(request.code());
        }
        if (request.nom() != null && !request.nom().isBlank()) {
            commune.setNom(request.nom());
        }
        if (request.territoireId() != null) {
            Territoire territoire = territoireRepository.findById(request.territoireId())
                    .orElseThrow(() -> new IllegalArgumentException("Territoire introuvable"));
            commune.setTerritoire(territoire);
        }
        if (request.actif() != null) {
            commune.setActif(request.actif());
        }

        return communeMapper.toResponse(communeRepository.save(commune));
    }
}
