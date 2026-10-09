package odk.malicompetences.backend.opportunite;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories-opportunites")
public class CategorieOpportuniteController {

    private final CategorieOpportuniteService service;

    public CategorieOpportuniteController(CategorieOpportuniteService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CategorieOpportuniteResponse creer(@Valid @RequestBody CategorieOpportuniteRequest request) {
        return service.creer(request);
    }

    @GetMapping
    public List<CategorieOpportuniteResponse> lister() {
        return service.listerToutes();
    }

    @GetMapping("/{id}")
    public CategorieOpportuniteResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}