package odk.malicompetences.backend.talent;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping({"/api/v1/suivi-talents", "/api/v1/suivi-besoins-talents"})
public class SuiviBesoinTalentController {

    private final SuiviBesoinTalentService service;

    public SuiviBesoinTalentController(SuiviBesoinTalentService service) {
        this.service = service;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuiviBesoinTalentResponse creer(@Valid @RequestBody SuiviBesoinTalentRequest request) {
        return service.creerSuivi(request);
    }
    @GetMapping("/{id}")
    public SuiviBesoinTalentResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }
    
    @GetMapping("/besoin/{besoinId}")
    public List<SuiviBesoinTalentResponse> listerParBesoin(@PathVariable UUID besoinId) {
        return service.listerParBesoin(besoinId);
    }
    @PutMapping("/{id}")
    public SuiviBesoinTalentResponse modifier(@PathVariable UUID id, @Valid @RequestBody SuiviBesoinTalentRequest request) {
        return service.modifierSuivi(id, request);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerSuivi(id);
    }
}
