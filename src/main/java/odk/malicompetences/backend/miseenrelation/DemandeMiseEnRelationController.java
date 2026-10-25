package odk.malicompetences.backend.miseenrelation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/demandes-mise-en-relation")
public class DemandeMiseEnRelationController {
    private final DemandeMiseEnRelationService service;
    public DemandeMiseEnRelationController(DemandeMiseEnRelationService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ORGANISATION','CITOYEN')")
    public DemandeMiseEnRelationResponse creer(@Valid @RequestBody DemandeMiseEnRelationRequest request) {
        return service.creerDemande(request);
    }

    @GetMapping @PreAuthorize("hasAnyRole('ORGANISATION','CITOYEN','ADMIN','SUPER_ADMIN')")
    public List<DemandeMiseEnRelationResponse> lister() { return service.listerToutes(); }

    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ORGANISATION','CITOYEN','ADMIN','SUPER_ADMIN')")
    public DemandeMiseEnRelationResponse trouverParId(@PathVariable UUID id) { return service.trouverParId(id); }

    @PatchMapping("/{id}/reponse") @PreAuthorize("hasRole('CITOYEN')")
    public DemandeMiseEnRelationResponse repondre(@PathVariable UUID id, @Valid @RequestBody ReponseMiseEnRelationRequest request) {
        return service.repondre(id, request.statut());
    }

    @PatchMapping("/{id}/message") @PreAuthorize("hasAnyRole('ORGANISATION','CITOYEN')")
    public DemandeMiseEnRelationResponse modifierMessage(@PathVariable UUID id, @Valid @RequestBody DemandeMiseEnRelationRequest request) {
        return service.modifierDemande(id, request);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ORGANISATION','CITOYEN','ADMIN','SUPER_ADMIN')")
    public void annuler(@PathVariable UUID id) { service.supprimerDemande(id); }

    public record ReponseMiseEnRelationRequest(@NotNull StatutContact statut) {}
}
