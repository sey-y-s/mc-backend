package odk.malicompetences.backend.besoin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/besoins/{besoinId}/competences")
@PreAuthorize("hasAnyRole('ORGANISATION','ADMIN','SUPER_ADMIN')")
public class BesoinCompetenceController {
    private final BesoinCompetenceService service;

    public BesoinCompetenceController(BesoinCompetenceService service) {
        this.service = service;
    }

    @GetMapping
    public List<BesoinCompetenceResponse> lister(@PathVariable UUID besoinId) {
        return service.listerCompetences(besoinId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BesoinCompetenceResponse ajouter(@PathVariable UUID besoinId,
                                            @Valid @RequestBody BesoinCompetenceRequest request) {
        return service.ajouterCompetence(besoinId, request);
    }

    @PutMapping("/{competenceId}")
    public BesoinCompetenceResponse modifier(@PathVariable UUID besoinId, @PathVariable UUID competenceId,
                                             @Valid @RequestBody BesoinCompetenceRequest request) {
        return service.modifierCompetence(besoinId, competenceId, request);
    }

    @DeleteMapping("/{competenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID besoinId, @PathVariable UUID competenceId) {
        service.supprimerCompetence(besoinId, competenceId);
    }
}
