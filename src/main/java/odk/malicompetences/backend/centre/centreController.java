package odk.malicompetences.backend.centre;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/centres")
public class CentreController {

    private final CentreService centreService;

    public CentreController(CentreService centreService) {
        this.centreService = centreService;
    }

    // Créer
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CentreResponse creer(@Valid @RequestBody CentreRequest request) {
        return centreService.creerCentre(request);
    }

    // Lister tout
    @GetMapping
    public List<CentreResponse> lister() {
        return centreService.listerTousLesCentres();
    }

    // Trouver par ID
    @GetMapping("/{id}")
    public CentreResponse trouverParId(@PathVariable UUID id) {
        return centreService.trouverCentreParId(id);
    }

    // Modifier
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public CentreResponse modifier(@PathVariable UUID id, @Valid @RequestBody CentreRequest request) {
        return centreService.modifierCentre(id, request);
    }

    // Supprimer
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        centreService.supprimerCentre(id);
    }
}
