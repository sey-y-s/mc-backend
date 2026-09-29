package odk.malicompetences.backend.portfolio;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioRealisationController {

    private final PortfolioRealisationService portfolioService;

    public PortfolioRealisationController(PortfolioRealisationService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<PortfolioRealisationResponse> creer(@Valid @RequestBody PortfolioRealisationRequest request) {
        return new ResponseEntity<>(portfolioService.creerPortfolio(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioRealisationResponse> trouverParId(@PathVariable UUID id) {
        return ResponseEntity.ok(portfolioService.trouverParId(id));
    }

    @GetMapping("/citoyen/{citoyenId}")
    public ResponseEntity<List<PortfolioRealisationResponse>> listerParCitoyen(@PathVariable UUID citoyenId) {
        return ResponseEntity.ok(portfolioService.listerParCitoyen(citoyenId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioRealisationResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody PortfolioRealisationRequest request
    ) {
        return ResponseEntity.ok(portfolioService.modifierPortfolio(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID id) {
        portfolioService.supprimerPortfolio(id);
        return ResponseEntity.noContent().build();
    }
}