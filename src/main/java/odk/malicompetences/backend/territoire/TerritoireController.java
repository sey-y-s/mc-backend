package odk.malicompetences.backend.territoire;

import jakarta.validation.Valid;
import odk.malicompetences.backend.common.pagination.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/territoires")
public class TerritoireController {

    private final TerritoireService service;

    public TerritoireController(TerritoireService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<TerritoireResponse> lister(
            @RequestParam(required = false) NiveauTerritoire niveau,
            @RequestParam(required = false) UUID parentId,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return service.lister(niveau, parentId, actif, q, pageable);
    }

    @GetMapping("/{id}")
    public TerritoireResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @GetMapping("/code/{code}")
    public TerritoireResponse trouverParCode(@PathVariable String code) {
        return service.trouverParCode(code);
    }

    @GetMapping("/{id}/enfants")
    public List<TerritoireResponse> listerEnfants(@PathVariable UUID id) {
        return service.listerEnfants(id);
    }

    @GetMapping("/{id}/communes")
    public List<CommuneResponse> listerCommunes(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean recursive
    ) {
        return service.listerCommunes(id, recursive);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TerritoireResponse creer(@Valid @RequestBody TerritoireRequest request) {
        return service.creerTerritoire(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public TerritoireResponse modifier(@PathVariable UUID id, @Valid @RequestBody TerritoireRequest request) {
        return service.modifierTerritoire(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{id}")
    public TerritoireResponse modifierPartiel(@PathVariable UUID id, @RequestBody TerritoireRequest request) {
        return service.patchTerritoire(id, request);
    }
}
