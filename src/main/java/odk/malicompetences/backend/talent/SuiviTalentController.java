package odk.malicompetences.backend.talent;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suivi-talents")
public class SuiviTalentController {

    private final SuiviTalentService service;

    public SuiviTalentController(SuiviTalentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuiviTalentResponse creer(@Valid @RequestBody SuiviTalentRequest request) {
        return service.creerSuivi(request);
    }

    @GetMapping("/{id}")
    public SuiviTalentResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @GetMapping("/besoin/{besoinId}")
    public List<SuiviTalentResponse> listerParBesoin(@PathVariable UUID besoinId) {
        return service.listerParBesoin(besoinId);
    }

    @PutMapping("/{id}")
    public SuiviTalentResponse modifier(@PathVariable UUID id, @Valid @RequestBody SuiviTalentRequest request) {
        return service.modifierSuivi(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerSuivi(id);
    }
}