package odk.malicompetences.backend.territoire;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/communes")
public class CommuneController {

    private final CommuneService service;

    public CommuneController(CommuneService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommuneResponse creer(@Valid @RequestBody CommuneRequest request) {
        return service.creerCommune(request);
    }

    @GetMapping
    public List<CommuneResponse> lister() {
        return service.listerToutes();
    }

    @GetMapping("/{id}")
    public CommuneResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @GetMapping("/region/{regionId}")
    public List<CommuneResponse> listerParRegion(@PathVariable UUID regionId) {
        return service.listerParRegion(regionId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")

    @PutMapping("/{id}")
    public CommuneResponse modifier(@PathVariable UUID id, @Valid @RequestBody CommuneRequest request) {
        return service.modifierCommune(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerCommune(id);
    }
}
