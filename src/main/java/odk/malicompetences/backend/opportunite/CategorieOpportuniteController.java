package odk.malicompetences.backend.opportunite;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}