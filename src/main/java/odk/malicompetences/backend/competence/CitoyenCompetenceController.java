package odk.malicompetences.backend.competence;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/citoyens/{citoyenId}/competences")
public class CitoyenCompetenceController {

    private final CitoyenCompetenceService service;

    public CitoyenCompetenceController(
            CitoyenCompetenceService service
    ) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitoyenCompetenceResponse ajouter(
            @PathVariable UUID citoyenId,
            @RequestBody CitoyenCompetenceRequest request
    ) {
        return service.ajouter(citoyenId, request);
    }

    @GetMapping
    public List<CitoyenCompetenceResponse> trouverParCitoyen(
            @PathVariable UUID citoyenId
    ) {
        return service.trouverParCitoyen(citoyenId);
    }
}