package odk.malicompetences.backend.besoin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping({"/api/besoins", "/api/v1/besoins"})
public class BesoinCompetenceController {

    private final BesoinCompetenceService besoinCompetenceService;

    public BesoinCompetenceController(BesoinCompetenceService besoinCompetenceService) {
        this.besoinCompetenceService = besoinCompetenceService;
    }

    // Créer un besoin avec ses compétences associées
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BesoinResponse creerBesoin(@Valid @RequestBody BesoinRequest request) {
        return besoinCompetenceService.creerBesoin(request);
    }

    // Lister tous les besoins
    @GetMapping
    public List<BesoinResponse> listerTousLesBesoins() {
        return besoinCompetenceService.listerTousLesBesoins();
    }

    // Trouver un besoin par son ID
    @GetMapping("/{id}")
    public BesoinResponse trouverBesoinParId(@PathVariable UUID id) {
        return besoinCompetenceService.trouverBesoinParId(id);
    }

    // Supprimer un besoin
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerBesoin(@PathVariable UUID id) {
        besoinCompetenceService.supprimerBesoin(id);
    }
}
