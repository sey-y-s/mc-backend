package odk.malicompetences.backend.validation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/citoyen-competences/{citoyenCompetenceId}/validations"
)
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(
            ValidationService validationService
    ) {
        this.validationService = validationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ValidationResponse creer(
            @PathVariable UUID citoyenCompetenceId,
            @RequestBody ValidationRequest request
    ) {
        return validationService.creer(
                citoyenCompetenceId,
                request
        );
    }

    @GetMapping
    public List<ValidationResponse> trouverParCompetence(
            @PathVariable UUID citoyenCompetenceId
    ) {
        return validationService.trouverParCompetence(
                citoyenCompetenceId
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ValidationResponse> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                validationService.getById(id)
        );
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<ValidationResponse> modifierStatut(
            @PathVariable UUID id,
            @RequestBody ValidationStatutRequest request) {

        return ResponseEntity.ok(
                validationService.modifierStatut(
                        id,
                        request.statut(),
                        request.commentaire()
                )
        );
    }
}