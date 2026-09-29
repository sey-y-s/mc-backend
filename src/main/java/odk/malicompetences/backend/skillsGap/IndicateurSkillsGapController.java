package odk.malicompetences.backend.skillsGap;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/indicateurs/skills-gap")
public class IndicateurSkillsGapController {

    private final IndicateurSkillsGapService service;

    public IndicateurSkillsGapController(IndicateurSkillsGapService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IndicateurSkillsGapResponse creer(@Valid @RequestBody IndicateurSkillsGapRequest request) {
        return service.creerIndicateur(request);
    }

    @GetMapping
    public List<IndicateurSkillsGapResponse> lister() {
        return service.listerTous();
    }

    @GetMapping("/{id}")
    public IndicateurSkillsGapResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @GetMapping("/region/{regionId}")
    public List<IndicateurSkillsGapResponse> listerParRegion(@PathVariable UUID regionId) {
        return service.listerParRegion(regionId);
    }

    @PutMapping("/{id}")
    public IndicateurSkillsGapResponse modifier(@PathVariable UUID id, @Valid @RequestBody IndicateurSkillsGapRequest request) {
        return service.modifierIndicateur(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerIndicateur(id);
    }
}