package odk.malicompetences.backend.competence;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Référentiel Compétences", description = "Consultation du catalogue national des compétences")
@RestController
@RequestMapping("/api/v1/competences")
public class CompetenceController {
    private final CompetenceService service;

    public CompetenceController(CompetenceService service) {
        this.service = service;
    }

    @Operation(summary = "Lister les compétences", description = "Filtrage facultatif par métier, secteur, statut et texte.")
    @GetMapping
    public List<CompetenceResponse> trouverToutes(
            @RequestParam(required = false) UUID metierId,
            @RequestParam(required = false) UUID secteurId,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String q
    ) {
        return service.trouverToutes(metierId, secteurId, actif, q);
    }

    @GetMapping("/{id}")
    public CompetenceResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @GetMapping("/code/{code}")
    public CompetenceResponse trouverParCode(@PathVariable String code) {
        return service.trouverParCode(code);
    }
}
