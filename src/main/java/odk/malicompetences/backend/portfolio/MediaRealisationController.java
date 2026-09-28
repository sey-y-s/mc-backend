package odk.malicompetences.backend.portfolio;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolio-realisations/{realisationId}/medias")
public class MediaRealisationController {

    private final MediaRealisationService service;

    public MediaRealisationController(
            MediaRealisationService service
    ) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MediaRealisationResponse ajouter(
            @PathVariable UUID realisationId,
            @RequestBody MediaRealisationRequest request
    ) {
        return service.ajouter(realisationId, request);
    }

    @GetMapping
    public List<MediaRealisationResponse> trouverParRealisation(
            @PathVariable UUID realisationId
    ) {
        return service.trouverParRealisation(realisationId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaRealisationResponse> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                service.getById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<MediaRealisationResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody MediaRealisationRequest request) {

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