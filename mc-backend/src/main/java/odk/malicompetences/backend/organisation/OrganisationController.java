package odk.malicompetences.backend.organisation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganisationResponse creer(@Valid @RequestBody OrganisationRequest request) {
        return service.creerOrganisation(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public List<OrganisationResponse> lister() {
        return service.listerToutes();
    }

    @PreAuthorize("#id.toString() == authentication.name or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{id}")
    public OrganisationResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public OrganisationResponse modifier(@PathVariable UUID id, @Valid @RequestBody OrganisationRequest request) {
        return service.modifierOrganisation(id, request);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerOrganisation(id);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ORGANISATION')")
    public OrganisationResponse monProfil(@AuthenticationPrincipal UserDetails principal) {
        return service.trouverParId(UUID.fromString(principal.getUsername()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ORGANISATION')")
    public OrganisationResponse modifierMonProfil(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody OrganisationProfilRequest request) {
        return service.modifierMonProfil(UUID.fromString(principal.getUsername()), request);
    }
}