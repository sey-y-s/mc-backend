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

    @PreAuthorize("hasRole('ORGANISATION')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganisationResponse creer(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody OrganisationRequest request
    ) {
        return service.creerOrganisation(UUID.fromString(principal.getUsername()), request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public List<OrganisationResponse> lister() {
        return service.listerToutes();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{id}")
    public OrganisationResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public OrganisationResponse modifier(@PathVariable UUID id, @Valid @RequestBody OrganisationRequest request) {
        return service.modifierOrganisation(id, request);
    }


    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public OrganisationResponse modifierStatut(@PathVariable UUID id, @Valid @RequestBody StatutOrganisationRequest request) {
        return service.modifierStatut(id, request.statut(), request.motif());
    }

    public record StatutOrganisationRequest(@jakarta.validation.constraints.NotNull StatutOrganisation statut,
                                           @jakarta.validation.constraints.Size(max = 2000) String motif) {}



    @GetMapping("/me/tableau-de-bord")
    @PreAuthorize("hasRole('ORGANISATION')")
    public OrganisationDashboard tableauDeBord(@AuthenticationPrincipal UserDetails principal) {
        UUID id = UUID.fromString(principal.getUsername());
        OrganisationResponse organisation = service.trouverMonProfil(id);
        return new OrganisationDashboard(
                organisation.statut() == null ? null : organisation.statut().name(),
                besoinRepository.countByOrganisation_IdAndStatut(organisation.id(), StatutBesoin.BROUILLON),
                besoinRepository.countByOrganisation_IdAndStatut(organisation.id(), StatutBesoin.OUVERT),
                besoinRepository.countByOrganisation_IdAndStatut(organisation.id(), StatutBesoin.CLOTURE)
        );
    }

    public record OrganisationDashboard(String statutOrganisation, long besoinsBrouillon,
                                        long besoinsOuverts, long besoinsClotures) {}

    @GetMapping("/me")
    @PreAuthorize("hasRole('ORGANISATION')")
    public OrganisationResponse monProfil(@AuthenticationPrincipal UserDetails principal) {
        return service.trouverMonProfil(UUID.fromString(principal.getUsername()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ORGANISATION')")
    public OrganisationResponse modifierMonProfil(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody OrganisationProfilRequest request) {
        return service.modifierMonProfil(UUID.fromString(principal.getUsername()), request);
    }
}