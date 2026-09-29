package odk.malicompetences.backend.territoire;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    private final RegionService service;

    public RegionController(RegionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegionResponse creer(@Valid @RequestBody RegionRequest request) {
        return service.creerRegion(request);
    }

    @GetMapping
    public List<RegionResponse> lister() {
        return service.listerToutes();
    }

    @GetMapping("/{id}")
    public RegionResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @PutMapping("/{id}")
    public RegionResponse modifier(@PathVariable UUID id, @Valid @RequestBody RegionRequest request) {
        return service.modifierRegion(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerRegion(id);
    }
}