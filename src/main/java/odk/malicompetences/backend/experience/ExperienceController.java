package odk.malicompetences.backend.experience;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/citoyens/{citoyenId}/experiences")
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

    @GetMapping("/{id}")
    public ResponseEntity<ExperienceResponse> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(experienceService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExperienceResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody ExperienceRequest request) {

        return ResponseEntity.ok(
                experienceService.modifier(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @PathVariable UUID id) {

        experienceService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

}