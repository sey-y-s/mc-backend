package odk.malicompetences.backend.opportunite;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpportuniteResponse creer(@Valid @RequestBody OpportuniteRequest request) {
        return service.creerOpportunite(request);
    }

    @GetMapping
    public List<OpportuniteResponse> lister() {
        return service.listerToutes();
    }

    @GetMapping("/{id}")
    public OpportuniteResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @PutMapping("/{id}")
    public OpportuniteResponse modifier(@PathVariable UUID id, @Valid @RequestBody OpportuniteRequest request) {
        return service.modifierOpportunite(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerOpportunite(id);
    }
}