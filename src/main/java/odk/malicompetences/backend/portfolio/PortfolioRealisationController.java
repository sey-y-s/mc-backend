package odk.malicompetences.backend.portfolio;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/citoyens/{citoyenId}/portfolio")
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
}