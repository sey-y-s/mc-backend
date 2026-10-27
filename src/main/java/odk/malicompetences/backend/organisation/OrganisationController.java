package odk.malicompetences.backend.organisation;

import jakarta.validation.Valid;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.besoin.StatutBesoin;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisations")
public class OrganisationController {

    private final OrganisationService service;
    private final BesoinRepository besoinRepository;

    public OrganisationController(OrganisationService service, BesoinRepository besoinRepository) {
        this.service = service;
        this.besoinRepository = besoinRepository;
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

    @GetMapping("/me/tableau-de-bord")
    @PreAuthorize("hasRole('ORGANISATION')")
    public OrganisationDashboard tableauDeBord(@AuthenticationPrincipal UserDetails principal) {
        UUID id = UUID.fromString(principal.getUsername());
        OrganisationResponse organisation = service.trouverParId(id);
        return new OrganisationDashboard(
                organisation.statut() == null ? null : organisation.statut().name(),
                besoinRepository.countByOrganisation_IdAndStatut(id, StatutBesoin.BROUILLON),
                besoinRepository.countByOrganisation_IdAndStatut(id, StatutBesoin.OUVERT),
                besoinRepository.countByOrganisation_IdAndStatut(id, StatutBesoin.CLOTURE)
        );
    }

    public record OrganisationDashboard(String statutOrganisation, long besoinsBrouillon,
                                        long besoinsOuverts, long besoinsClotures) {}

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