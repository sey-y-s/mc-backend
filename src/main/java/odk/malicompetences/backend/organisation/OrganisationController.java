package odk.malicompetences.backend.organisation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organisations")
public class OrganisationController {

    private final OrganisationService service;

    public OrganisationController(OrganisationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganisationResponse creer(@Valid @RequestBody OrganisationRequest request) {
        return service.creerOrganisation(request);
    }

    @GetMapping
    public List<OrganisationResponse> lister() {
        return service.listerToutes();
    }

    @GetMapping("/{id}")
    public OrganisationResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @PutMapping("/{id}")
    public OrganisationResponse modifier(@PathVariable UUID id, @Valid @RequestBody OrganisationRequest request) {
        return service.modifierOrganisation(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerOrganisation(id);
    }
}