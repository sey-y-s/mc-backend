package odk.malicompetences.backend.institution;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/** Indicateurs calculés à la demande, sans stockage d'instantanés. */
@RestController
@RequestMapping("/api/v1/territoires/indicateurs")
@PreAuthorize("hasRole('INSTITUTION')")
public class TerritoireIndicateurController {
    private final InstitutionIndicateurService service;
    public TerritoireIndicateurController(InstitutionIndicateurService service) { this.service = service; }

    @GetMapping
    public TerritoireIndicateursResponse lister(@RequestParam UUID competenceId) {
        List<InstitutionIndicateurResponse> rows = service.lire(competenceId);
        return new TerritoireIndicateursResponse(competenceId, rows.isEmpty() ? null : rows.get(0).dateCalcul(), rows);
    }

    public record TerritoireIndicateursResponse(UUID competenceId, java.time.LocalDateTime dateCalcul,
                                                 List<InstitutionIndicateurResponse> items) {}
}
