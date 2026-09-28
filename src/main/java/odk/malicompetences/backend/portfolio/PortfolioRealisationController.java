package odk.malicompetences.backend.portfolio;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/citoyens/{citoyenId}/portfolio")
public class PortfolioRealisationController {

    private final PortfolioRealisationService service;

    public PortfolioRealisationController(
            PortfolioRealisationService service
    ) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioRealisationResponse ajouter(
            @PathVariable UUID citoyenId,
            @RequestBody PortfolioRealisationRequest request
    ) {
        return service.ajouter(citoyenId, request);
    }

    @GetMapping
    public List<PortfolioRealisationResponse> trouverParCitoyen(
            @PathVariable UUID citoyenId
    ) {
        return service.trouverParCitoyen(citoyenId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioRealisationResponse> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                service.getById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioRealisationResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody PortfolioRealisationRequest request) {

        return ResponseEntity.ok(
                service.modifier(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @PathVariable UUID id) {

        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}