package odk.malicompetences.backend.territoire;

import jakarta.validation.Valid;
import odk.malicompetences.backend.common.pagination.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/communes")
public class CommuneController {

    private final CommuneService service;

    public CommuneController(CommuneService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<CommuneResponse> lister(
            @RequestParam(required = false) UUID territoireId,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return service.lister(territoireId, actif, q, pageable);
    }

    @GetMapping("/{id}")
    public CommuneResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @GetMapping("/code/{code}")
    public CommuneResponse trouverParCode(@PathVariable String code) {
        return service.trouverParCode(code);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommuneResponse creer(@Valid @RequestBody CommuneRequest request) {
        return service.creerCommune(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public CommuneResponse modifier(@PathVariable UUID id, @Valid @RequestBody CommuneRequest request) {
        return service.modifierCommune(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{id}")
    public CommuneResponse modifierPartiel(@PathVariable UUID id, @RequestBody CommuneRequest request) {
        return service.patchCommune(id, request);
    }
}
