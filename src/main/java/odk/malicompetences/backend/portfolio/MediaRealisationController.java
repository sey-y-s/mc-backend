package odk.malicompetences.backend.portfolio;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio-realisations/{realisationId}/medias")
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
}