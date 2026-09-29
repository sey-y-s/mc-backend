package odk.malicompetences.backend.preuve;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/preuves")
public class PreuveController {

    private final PreuveService service;

    public PreuveController(PreuveService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PreuveResponse creer(@Valid @RequestBody PreuveRequest request) {
        return service.creerPreuve(request);
    }

    @GetMapping("/{id}")
    public PreuveResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @GetMapping("/citoyen-competence/{citoyenCompetenceId}")
    public List<PreuveResponse> listerParCitoyenCompetence(@PathVariable UUID citoyenCompetenceId) {
        return service.listerParCitoyenCompetence(citoyenCompetenceId);
    }

    @PutMapping("/{id}")
    public PreuveResponse modifier(@PathVariable UUID id, @Valid @RequestBody PreuveRequest request) {
        return service.modifierPreuve(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerPreuve(id);
    }
}