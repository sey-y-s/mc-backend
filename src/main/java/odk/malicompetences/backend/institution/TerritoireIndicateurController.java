package odk.malicompetences.backend.institution;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Contrat agrégé pour la carte choroplèthe. La jointure frontend doit utiliser regionCode.
 */
@RestController
@RequestMapping("/api/v1/territoires/indicateurs")
@PreAuthorize("hasRole('INSTITUTION')")
public class TerritoireIndicateurController {
    private final InstitutionIndicateurService service;
    private final InstitutionFeatureGate gate;

    public TerritoireIndicateurController(InstitutionIndicateurService service, InstitutionFeatureGate gate) {
        this.service = service;
        this.gate = gate;
    }

    @GetMapping
    public TerritoireIndicateursResponse lister(
            @RequestParam UUID competenceId,
            @RequestParam(required = false) LocalDate periodeDebut,
            @RequestParam(required = false) LocalDate periodeFin,
            @RequestParam(defaultValue = "ECART") MesureTerritoriale mesure
    ) {
        gate.requireEnabled();
        List<InstitutionIndicateurResponse> rows = service.lire(competenceId, periodeDebut, periodeFin);
        return new TerritoireIndicateursResponse(competenceId, periodeDebut, periodeFin, mesure, rows);
    }

    public record TerritoireIndicateursResponse(
            UUID competenceId,
            LocalDate periodeDebut,
            LocalDate periodeFin,
            MesureTerritoriale mesure,
            List<InstitutionIndicateurResponse> items
    ) {}
}
