package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.competence.NiveauCompetence;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@PreAuthorize("hasAnyRole('ORGANISATION', 'ADMIN', 'SUPER_ADMIN')")
@RequestMapping("/api/v1/talents")
public class TalentRechercheController {

    private final TalentRechercheService service;

    public TalentRechercheController(TalentRechercheService service) {
        this.service = service;
    }

    /**
     * Recherche de talents. Paramètres textuels historiques (competence, metier, localisation, disponibilite)
     * et paramètres du mobile (q, competenceId, metierId, regionId, communeId, niveauMin, page, size).
     * Tous sont facultatifs ; page/size limitent la liste renvoyée.
     */
    @GetMapping("/recherche")
    public List<TalentRechercheResponse> rechercher(
            @RequestParam(required = false) String competence,
            @RequestParam(required = false) String metier,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) String disponibilite,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID competenceId,
            @RequestParam(required = false) UUID metierId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) UUID communeId,
            @RequestParam(required = false) String niveauMin,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        NiveauCompetence niveau = (niveauMin == null || niveauMin.isBlank())
                ? null
                : NiveauCompetence.valueOf(niveauMin.trim().toUpperCase(Locale.ROOT));
        TalentFiltres filtres = new TalentFiltres(competence, metier, localisation, disponibilite,
                q, competenceId, metierId, regionId, communeId, niveau);
        return service.rechercher(filtres, page, size);
    }

    @GetMapping("/{id}")
    public TalentRechercheResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }
}
