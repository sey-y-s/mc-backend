package odk.malicompetences.backend.competence;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/metiers")
public class MetierController {
    private final MetierService service;

    public MetierController(MetierService service) {
        this.service = service;
    }

    @GetMapping
    public List<MetierResponse> lister(
            @RequestParam(required = false) UUID secteurId,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String q
    ) {
        return service.lister(secteurId, actif, q);
    }

    @GetMapping("/{id}")
    public MetierResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @GetMapping("/code/{code}")
    public MetierResponse trouverParCode(@PathVariable String code) {
        return service.trouverParCode(code);
    }
}
