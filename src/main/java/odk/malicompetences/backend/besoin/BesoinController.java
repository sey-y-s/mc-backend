package odk.malicompetences.backend.besoin;


import jakarta.validation.Valid;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/besoins")
public class BesoinController {
    private final BesoinCompetenceService service;
    private final BesoinRepository besoinRepository;
    private final OrganisationRepository organisationRepository;

    public BesoinController(BesoinCompetenceService service, BesoinRepository besoinRepository,
                            OrganisationRepository organisationRepository) {
        this.service = service;
        this.besoinRepository = besoinRepository;
        this.organisationRepository = organisationRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ORGANISATION','ADMIN','SUPER_ADMIN')")
    public List<BesoinResponse> lister(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return service.listerTousLesBesoins();
        }
        UUID utilisateurId = UUID.fromString(authentication.getName());
        UUID organisationId = organisationRepository.findByUtilisateur_Id(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Profil organisation introuvable.")).getId();
        return service.listerParOrganisation(organisationId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANISATION','ADMIN','SUPER_ADMIN')")
    public BesoinResponse lire(@PathVariable UUID id, Authentication authentication) {
        BesoinResponse response = service.trouverBesoinParId(id);
        if (authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) return response;
        UUID utilisateurId = UUID.fromString(authentication.getName());
        UUID organisationId = organisationRepository.findByUtilisateur_Id(utilisateurId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Profil organisation introuvable.")).getId();
        if (!organisationId.equals(response.organisationId())) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé.");
        }
        return response;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ORGANISATION')")
    public BesoinResponse creer(@Valid @RequestBody BesoinRequest request) {
        return service.creerBesoin(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANISATION','ADMIN','SUPER_ADMIN')")
    public BesoinResponse modifier(@PathVariable UUID id, @Valid @RequestBody BesoinRequest request) {
        return service.modifierBesoin(id, request);
    }

    @PatchMapping("/{id}/cloture")
    @PreAuthorize("hasAnyRole('ORGANISATION','ADMIN','SUPER_ADMIN')")
    public BesoinResponse cloturer(@PathVariable UUID id) {
        return service.cloturerBesoin(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ORGANISATION','ADMIN','SUPER_ADMIN')")
    public void supprimer(@PathVariable UUID id) {
        service.supprimerBesoin(id);
    }

    @GetMapping("/me/tableau-de-bord")
    @PreAuthorize("hasRole('ORGANISATION')")
    public OrganisationDashboard dashboard(Authentication authentication) {
        UUID utilisateurId = UUID.fromString(authentication.getName());
        var organisation = organisationRepository.findByUtilisateur_Id(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Profil organisation introuvable."));
        UUID organisationId = organisation.getId();
        return new OrganisationDashboard(organisation.getStatut().name(),
                besoinRepository.countByOrganisation_IdAndStatut(organisationId, StatutBesoin.BROUILLON),
                besoinRepository.countByOrganisation_IdAndStatut(organisationId, StatutBesoin.OUVERT),
                besoinRepository.countByOrganisation_IdAndStatut(organisationId, StatutBesoin.CLOTURE));
    }

    public record OrganisationDashboard(String statutOrganisation, long besoinsBrouillon, long besoinsOuverts,
                                        long besoinsClotures) {}
}
