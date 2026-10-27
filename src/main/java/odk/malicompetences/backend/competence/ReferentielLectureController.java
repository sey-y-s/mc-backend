package odk.malicompetences.backend.competence;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Lecture publique des secteurs d'activité et des métiers (formulaires d'inscription, filtres, recherche). */
@Tag(name = "Référentiel", description = "Secteurs d'activité et métiers, en lecture seule")
@RestController
@RequestMapping("/api/v1")
public class ReferentielLectureController {

    private final ReferentielLectureService service;

    public ReferentielLectureController(ReferentielLectureService service) {
        this.service = service;
    }

    @Operation(summary = "Lister les secteurs d'activité")
    @GetMapping("/secteurs-activite")
    public List<SecteurActiviteResume> secteurs() {
        return service.secteurs();
    }

    @Operation(summary = "Consulter un secteur d'activité")
    @GetMapping("/secteurs-activite/{id}")
    public SecteurActiviteResume secteur(@PathVariable UUID id) {
        return service.secteur(id);
    }

    @Operation(summary = "Lister les métiers", description = "Filtres facultatifs : secteur et fragment du nom.")
    @GetMapping("/metiers")
    public List<MetierResume> metiers(@RequestParam(required = false) UUID secteurId,
                                      @RequestParam(required = false) String q) {
        return service.metiers(secteurId, q);
    }

    @Operation(summary = "Consulter un métier")
    @GetMapping("/metiers/{id}")
    public MetierResume metier(@PathVariable UUID id) {
        return service.metier(id);
    }
}
