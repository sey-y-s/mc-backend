package odk.malicompetences.backend.territoire;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/communes")
public class CommuneController {

    private final CommuneService service;

    public CommuneController(CommuneService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommuneResponse creer(@Valid @RequestBody CommuneRequest request) {
        return service.creerCommune(request);
    }

    @GetMapping
    public List<CommuneResponse> lister() {
        return service.listerToutes();
    }

    @GetMapping("/{id}")
    public CommuneResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @GetMapping("/region/{regionId}")
    public List<CommuneResponse> listerParRegion(@PathVariable UUID regionId) {
        return service.listerParRegion(regionId);
    }

    @PutMapping("/{id}")
    public CommuneResponse modifier(@PathVariable UUID id, @Valid @RequestBody CommuneRequest request) {
        return service.modifierCommune(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerCommune(id);
    }
}