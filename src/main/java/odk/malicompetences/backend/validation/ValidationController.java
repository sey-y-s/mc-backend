package odk.malicompetences.backend.validation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/validations")
public class ValidationController {

    private final ValidationService service;

    public ValidationController(ValidationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ValidationResponse creer(@Valid @RequestBody ValidationRequest request) {
        return service.creerValidation(request);
    }

    @GetMapping
    public List<ValidationResponse> lister() {
        return service.listerToutes();
    }

    @GetMapping("/{id}")
    public ValidationResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @GetMapping("/citoyen-competence/{citoyenCompetenceId}")
    public List<ValidationResponse> listerParCitoyenCompetence(@PathVariable UUID citoyenCompetenceId) {
        return service.listerParCitoyenCompetence(citoyenCompetenceId);
    }

    @PutMapping("/{id}")
    public ValidationResponse modifier(@PathVariable UUID id, @Valid @RequestBody ValidationRequest request) {
        return service.modifierValidation(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerValidation(id);
    }
}