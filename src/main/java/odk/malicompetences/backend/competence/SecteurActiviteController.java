package odk.malicompetences.backend.competence;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/secteurs-activite")
public class SecteurActiviteController {
    private final SecteurActiviteService service;

    public SecteurActiviteController(SecteurActiviteService service) {
        this.service = service;
    }

    @GetMapping
    public List<SecteurActiviteResponse> lister(@RequestParam(required = false) Boolean actif) {
        return service.lister(actif);
    }

    @GetMapping("/{id}")
    public SecteurActiviteResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @GetMapping("/code/{code}")
    public SecteurActiviteResponse trouverParCode(@PathVariable String code) {
        return service.trouverParCode(code);
    }
}
