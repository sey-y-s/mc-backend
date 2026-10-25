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
public class TerritoireService {

    private final TerritoireRepository territoireRepository;
    private final CommuneRepository communeRepository;
    private final TerritoireMapper territoireMapper;
    private final CommuneMapper communeMapper;

    public TerritoireService(
            TerritoireRepository territoireRepository,
            CommuneRepository communeRepository,
            TerritoireMapper territoireMapper,
            CommuneMapper communeMapper
    ) {
        this.territoireRepository = territoireRepository;
        this.communeRepository = communeRepository;
        this.territoireMapper = territoireMapper;
        this.communeMapper = communeMapper;
    }

    public TerritoireResponse creerTerritoire(TerritoireRequest request) {
        if (territoireRepository.existsByCode(request.code())) {
            throw new IllegalStateException("Ce code territorial est déjà utilisé");
        }

        Territoire parent = null;
        if (request.parentId() != null) {
            parent = territoireRepository.findById(request.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("Territoire parent introuvable"));
        }

        validerHierarchie(request.niveau(), parent);

        Territoire territoire = territoireMapper.toEntity(request);
        territoire.setParent(parent);

        return territoireMapper.toResponse(territoireRepository.save(territoire));
    }

    @Transactional(readOnly = true)
    public PageResponse<TerritoireResponse> lister(
            NiveauTerritoire niveau,
            UUID parentId,
            Boolean actif,
            String q,
            Pageable pageable
    ) {
        Page<Territoire> page = territoireRepository.rechercher(niveau, parentId, actif, q, pageable);
        return PageResponse.from(page, territoireMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TerritoireResponse> listerToutes() {
        return territoireRepository.findAll().stream()
                .map(territoireMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TerritoireResponse trouverParId(UUID id) {
        Territoire territoire = territoireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Territoire introuvable : " + id));
        return territoireMapper.toResponse(territoire);
    }

    @Transactional(readOnly = true)
    public TerritoireResponse trouverParCode(String code) {
        Territoire territoire = territoireRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Territoire introuvable pour le code : " + code));
        return territoireMapper.toResponse(territoire);
    }

    @Transactional(readOnly = true)
    public List<TerritoireResponse> listerEnfants(UUID parentId) {
        return territoireRepository.findAllByParentId(parentId).stream()
                .map(territoireMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommuneResponse> listerCommunes(UUID territoireId, boolean recursive) {
        if (recursive) {
            return communeRepository.findCommunesRecursive(territoireId).stream()
                    .map(communeMapper::toResponse)
                    .collect(Collectors.toList());
        } else {
            return communeRepository.findAllByTerritoireId(territoireId).stream()
                    .map(communeMapper::toResponse)
                    .collect(Collectors.toList());
        }
    }

    public TerritoireResponse modifierTerritoire(UUID id, TerritoireRequest request) {
        Territoire territoire = territoireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Territoire introuvable : " + id));

        if (!territoire.getCode().equals(request.code()) && territoireRepository.existsByCode(request.code())) {
            throw new IllegalStateException("Ce code territorial est déjà utilisé");
        }

        Territoire parent = null;
        if (request.parentId() != null) {
            parent = territoireRepository.findById(request.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("Territoire parent introuvable"));
        }

        validerHierarchie(request.niveau(), parent);

        territoire.setCode(request.code());
        territoire.setNom(request.nom());
        territoire.setNiveau(request.niveau());
        territoire.setParent(parent);
        if (request.actif() != null) {
            territoire.setActif(request.actif());
        }

        return territoireMapper.toResponse(territoireRepository.save(territoire));
    }

    public TerritoireResponse patchTerritoire(UUID id, TerritoireRequest request) {
        Territoire territoire = territoireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Territoire introuvable : " + id));

        if (request.code() != null && !request.code().isBlank() && !request.code().equals(territoire.getCode())) {
            if (territoireRepository.existsByCode(request.code())) {
                throw new IllegalStateException("Ce code territorial est déjà utilisé");
            }
            territoire.setCode(request.code());
        }
        if (request.nom() != null && !request.nom().isBlank()) {
            territoire.setNom(request.nom());
        }
        if (request.actif() != null) {
            territoire.setActif(request.actif());
        }
        if (request.niveau() != null || request.parentId() != null) {
            NiveauTerritoire niveau = request.niveau() != null ? request.niveau() : territoire.getNiveau();
            Territoire parent = request.parentId() != null
                    ? territoireRepository.findById(request.parentId()).orElseThrow(() -> new IllegalArgumentException("Parent introuvable"))
                    : territoire.getParent();
            validerHierarchie(niveau, parent);
            territoire.setNiveau(niveau);
            territoire.setParent(parent);
        }

        return territoireMapper.toResponse(territoireRepository.save(territoire));
    }

    private void validerHierarchie(NiveauTerritoire niveau, Territoire parent) {
        if (niveau == NiveauTerritoire.REGION || niveau == NiveauTerritoire.DISTRICT) {
            if (parent != null) {
                throw new IllegalArgumentException("Une région ou un district ne peut pas avoir de parent territorial.");
            }
        } else if (niveau == NiveauTerritoire.CERCLE) {
            if (parent == null) {
                throw new IllegalArgumentException("Un cercle doit obligatoirement avoir une région pour parent.");
            }
            if (parent.getNiveau() != NiveauTerritoire.REGION) {
                throw new IllegalArgumentException("Le parent d'un cercle doit être de niveau REGION (obtenu : " + parent.getNiveau() + ").");
            }
        }
    }
}
