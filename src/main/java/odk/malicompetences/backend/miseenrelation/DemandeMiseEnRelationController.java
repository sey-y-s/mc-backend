package odk.malicompetences.backend.miseenrelation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/demandes-mise-en-relation")
public class DemandeMiseEnRelationController {

    private final DemandeMiseEnRelationService service;

    public DemandeMiseEnRelationController(DemandeMiseEnRelationService service) {
        this.service = service;
    }

    // Créer
    @PostMapping
    @PreAuthorize("hasAnyRole('CITOYEN', 'ORGANISATION')")
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeMiseEnRelationResponse creer(@Valid @RequestBody DemandeMiseEnRelationRequest request) {
        return service.creerDemande(request);
    }

    // Lister tout
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public List<DemandeMiseEnRelationResponse> lister() {
        return service.listerToutes();
    }

    // Demandes de l'utilisateur connecté : reçues (citoyen) ou envoyées (organisation)
    @PreAuthorize("hasAnyRole('CITOYEN', 'ORGANISATION')")
    @GetMapping("/mes-demandes")
    public List<DemandeMiseEnRelationResponse> mesDemandes() {
        return service.listerMesDemandes();
    }

    // Le citoyen destinataire accepte ou refuse la demande
    @PreAuthorize("hasRole('CITOYEN')")
    @PatchMapping("/{id}/reponse")
    public DemandeMiseEnRelationResponse repondre(@PathVariable UUID id, @Valid @RequestBody ReponseMiseEnRelationRequest request) {
        return service.repondre(id, request.statut());
    }

    // L'organisation retire sa demande tant qu'elle est en attente
        @PreAuthorize("hasAnyRole('CITOYEN', 'ORGANISATION')")
    @PatchMapping("/{id}/annulation")
    public DemandeMiseEnRelationResponse annuler(@PathVariable UUID id) {
        return service.annuler(id);
    }

    // Trouver par ID
    @GetMapping("/{id}")
    public DemandeMiseEnRelationResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    // Modifier (ex: changer le statut en ACCEPTEE ou REFUSEE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public DemandeMiseEnRelationResponse modifier(@PathVariable UUID id, @Valid @RequestBody DemandeMiseEnRelationRequest request) {
        return service.modifierDemande(id, request);
    }

    // Supprimer
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerDemande(id);
    }
}
