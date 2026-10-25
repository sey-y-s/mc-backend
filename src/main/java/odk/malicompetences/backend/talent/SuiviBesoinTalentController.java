package odk.malicompetences.backend.talent;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/suivi-besoins-talents")
public class SuiviBesoinTalentController {

    private final SuiviBesoinTalentService service;

    public SuiviBesoinTalentController(SuiviBesoinTalentService service) {
        this.service = service;
    }
    @PostMapping
    @PreAuthorize("hasRole('ORGANISATION')")
    @ResponseStatus(HttpStatus.CREATED)
    public SuiviBesoinTalentResponse creer(@Valid @RequestBody SuiviBesoinTalentRequest request) {
        return service.creerSuivi(request);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANISATION','CITOYEN','ADMIN','SUPER_ADMIN')")
    public SuiviBesoinTalentResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }
    
    @GetMapping("/besoin/{besoinId}")
    @PreAuthorize("hasAnyRole('ORGANISATION','ADMIN','SUPER_ADMIN')")
    public List<SuiviBesoinTalentResponse> listerParBesoin(@PathVariable UUID besoinId) {
        return service.listerParBesoin(besoinId);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANISATION')")
    public SuiviBesoinTalentResponse modifier(@PathVariable UUID id, @Valid @RequestBody SuiviBesoinTalentRequest request) {
        return service.modifierSuivi(id, request);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANISATION','ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerSuivi(id);
    }
}
