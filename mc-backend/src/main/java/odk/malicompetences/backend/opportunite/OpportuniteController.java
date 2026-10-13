package odk.malicompetences.backend.opportunite;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/opportunites")
public class OpportuniteController {

    private final OpportuniteService service;

    public OpportuniteController(OpportuniteService service) {
        this.service = service;
    }

    /** Lecture publique : la liste ne contient que les opportunités visibles. */
    @GetMapping
    public List<OpportuniteResponse> lister() {
        return service.listerToutes();
    }

    /** Lecture publique : les brouillons, archives et annonces expirées restent masqués. */
    @GetMapping("/{id}")
    public OpportuniteResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    /** Liste complète pour l'administration, y compris les brouillons et archives. */
    @GetMapping("/gestion")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<OpportuniteResponse> listerPourGestion() {
        return service.listerPourGestion();
    }

    @GetMapping("/gestion/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public OpportuniteResponse trouverPourGestion(@PathVariable UUID id) {
        return service.trouverPourGestion(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public OpportuniteResponse creer(@Valid @RequestBody OpportuniteRequest request) {
        return service.creerOpportunite(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public OpportuniteResponse modifier(@PathVariable UUID id, @Valid @RequestBody OpportuniteRequest request) {
        return service.modifierOpportunite(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void supprimer(@PathVariable UUID id) {
        service.supprimerOpportunite(id);
    }
}
