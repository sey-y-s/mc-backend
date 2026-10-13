package odk.malicompetences.backend.centre;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping({"/api/centres", "/api/v1/centres"})
public class centreController {

    private final centerService centerService;

    public centreController(centerService centerService) {
        this.centerService = centerService;
    }

    // Créer
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CenterResponse creer(@Valid @RequestBody CentreRequest request) {
        return centerService.creerCentre(request);
    }

    // Lister tout
    @GetMapping
    public List<CenterResponse> lister() {
        return centerService.listerTousLesCentres();
    }

    // Trouver par ID
    @GetMapping("/{id}")
    public CenterResponse trouverParId(@PathVariable UUID id) {
        return centerService.trouverCentreParId(id);
    }

    // Modifier
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public CenterResponse modifier(@PathVariable UUID id, @Valid @RequestBody CentreRequest request) {
        return centerService.modifierCentre(id, request);
    }

    // Supprimer
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        centerService.supprimerCentre(id);
    }
}
