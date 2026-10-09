package odk.malicompetences.backend.skillsGap;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping({"/api/indicateurs/skills-gap", "/api/v1/indicateurs/skills-gap"})
public class IndicateurSkillsGapController {

    private final IndicateurSkillsGapService service;

    public IndicateurSkillsGapController(IndicateurSkillsGapService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IndicateurSkillsGapResponse creer(@Valid @RequestBody IndicateurSkillsGapRequest request) {
        return service.creerIndicateur(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")

    @GetMapping
    public List<IndicateurSkillsGapResponse> lister() {
        return service.listerTous();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")

    @GetMapping("/{id}")
    public IndicateurSkillsGapResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")

    @GetMapping("/region/{regionId}")
    public List<IndicateurSkillsGapResponse> listerParRegion(@PathVariable UUID regionId) {
        return service.listerParRegion(regionId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")

    @PutMapping("/{id}")
    public IndicateurSkillsGapResponse modifier(@PathVariable UUID id, @Valid @RequestBody IndicateurSkillsGapRequest request) {
        return service.modifierIndicateur(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerIndicateur(id);
    }
}
