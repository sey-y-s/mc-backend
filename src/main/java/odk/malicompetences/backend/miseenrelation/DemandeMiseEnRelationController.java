package odk.malicompetences.backend.miseenrelation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/demandes-mise-en-relation")
public class DemandeMiseEnRelationController {

    private final DemandeMiseEnRelationService service;

    public DemandeMiseEnRelationController(DemandeMiseEnRelationService service) {
        this.service = service;
    }

    // Créer
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeMiseEnRelationResponse creer(@Valid @RequestBody DemandeMiseEnRelationRequest request) {
        return service.creerDemande(request);
    }

    // Lister tout
    @GetMapping
    public List<DemandeMiseEnRelationResponse> lister() {
        return service.listerToutes();
    }

    // Trouver par ID
    @GetMapping("/{id}")
    public DemandeMiseEnRelationResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    // Modifier (ex: changer le statut en ACCEPTEE ou REFUSEE)
    @PutMapping("/{id}")
    public DemandeMiseEnRelationResponse modifier(@PathVariable UUID id, @Valid @RequestBody DemandeMiseEnRelationRequest request) {
        return service.modifierDemande(id, request);
    }

    // Supprimer
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerDemande(id);
    }
}