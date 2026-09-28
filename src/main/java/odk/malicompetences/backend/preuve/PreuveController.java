package odk.malicompetences.backend.preuve;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/citoyen-competences/{citoyenCompetenceId}/preuves")
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

    @GetMapping("/{id}")
    public ResponseEntity<PreuveResponse> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                preuveService.getById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PreuveResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody PreuveRequest request) {

        return ResponseEntity.ok(
                preuveService.modifier(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @PathVariable UUID id) {

        preuveService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}