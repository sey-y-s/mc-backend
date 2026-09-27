package odk.malicompetences.backend.experience;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/citoyens/{citoyenId}/experiences")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(
            ExperienceService experienceService
    ) {
        this.experienceService = experienceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExperienceResponse ajouter(
            @PathVariable UUID citoyenId,
            @RequestBody ExperienceRequest request
    ) {
        return experienceService.ajouter(citoyenId, request);
    }

    @GetMapping
    public List<ExperienceResponse> trouverParCitoyen(
            @PathVariable UUID citoyenId
    ) {
        return experienceService.trouverParCitoyen(citoyenId);
    }
}