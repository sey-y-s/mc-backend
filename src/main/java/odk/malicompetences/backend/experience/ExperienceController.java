package odk.malicompetences.backend.experience;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/experiences")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExperienceResponse creer(@Valid @RequestBody ExperienceRequest request) {
        return experienceService.creerExperience(request);
    }

    @GetMapping
    public List<ExperienceResponse> lister() {
        return experienceService.listerToutes();
    }

    @GetMapping("/{id}")
    public ExperienceResponse trouverParId(@PathVariable UUID id) {
        return experienceService.trouverParId(id);
    }

    @GetMapping("/citoyen/{citoyenId}")
    public List<ExperienceResponse> listerParCitoyen(@PathVariable UUID citoyenId) {
        return experienceService.listerParCitoyen(citoyenId);
    }

    @PutMapping("/{id}")
    public ExperienceResponse modifier(@PathVariable UUID id, @Valid @RequestBody ExperienceRequest request) {
        return experienceService.modifierExperience(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        experienceService.supprimerExperience(id);
    }
}