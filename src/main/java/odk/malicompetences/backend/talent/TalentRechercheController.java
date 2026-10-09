package odk.malicompetences.backend.talent;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/talents")
public class TalentRechercheController {

    private final TalentRechercheService service;

    public TalentRechercheController(TalentRechercheService service) {
        this.service = service;
    }

    @GetMapping("/recherche")
    public List<TalentRechercheResponse> rechercher(
            @RequestParam(required = false) String competence,
            @RequestParam(required = false) String metier,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) String disponibilite
    ) {
        return service.rechercher(competence, metier, localisation, disponibilite);
    }

    @GetMapping("/{id}")
    public TalentRechercheResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }
}
