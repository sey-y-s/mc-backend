package odk.malicompetences.backend.preuve;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/citoyen-competences/{citoyenCompetenceId}/preuves")
public class PreuveController {

    private final PreuveService preuveService;

    public PreuveController(PreuveService preuveService) {
        this.preuveService = preuveService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PreuveResponse ajouter(
            @PathVariable UUID citoyenCompetenceId,
            @RequestBody PreuveRequest request
    ) {
        return preuveService.ajouter(
                citoyenCompetenceId,
                request
        );
    }

    @GetMapping
    public List<PreuveResponse> trouverParCompetence(
            @PathVariable UUID citoyenCompetenceId
    ) {
        return preuveService.trouverParCompetence(
                citoyenCompetenceId
        );
    }
}