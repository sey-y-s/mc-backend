package odk.malicompetences.backend.validation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/citoyen-competences/{citoyenCompetenceId}/validations"
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
}