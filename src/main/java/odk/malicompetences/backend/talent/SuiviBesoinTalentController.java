package odk.malicompetences.backend.talent;

import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suivi-talents")
public class SuiviBesoinTalentController {

    private final SuiviBesoinTalentService service;

    public SuiviBesoinTalentController(SuiviBesoinTalentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuiviBesoinTalentResponse creer(
            @Valid @RequestBody SuiviBesoinTalentRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        verifierOrganisation(request.organisationId(), user);
        return service.creerSuivi(request);
    }

    @GetMapping("/organisation")
    public List<SuiviBesoinTalentResponse> listerPourOrganisation(
            @AuthenticationPrincipal UserDetails user
    ) {
        return service.listerParOrganisation(UUID.fromString(user.getUsername()));
    }

    @GetMapping("/{id}")
    public SuiviBesoinTalentResponse trouverParId(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails user
    ) {
        SuiviBesoinTalentResponse suivi = service.trouverParId(id);
        verifierOrganisation(suivi.organisationId(), user);
        return suivi;
    }

    @GetMapping("/besoin/{besoinId}")
    public List<SuiviBesoinTalentResponse> listerParBesoin(
            @PathVariable UUID besoinId,
            @AuthenticationPrincipal UserDetails user
    ) {
        if (!estAdministrateur(user)) {
            service.verifierBesoinEtOrganisation(besoinId, UUID.fromString(user.getUsername()));
        }
        return service.listerParBesoin(besoinId);
    }

    @PutMapping("/{id}")
    public SuiviBesoinTalentResponse modifier(
            @PathVariable UUID id,
            @Valid @RequestBody SuiviBesoinTalentRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        verifierOrganisation(request.organisationId(), user);
        verifierOrganisation(service.trouverParId(id).organisationId(), user);
        return service.modifierSuivi(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id, @AuthenticationPrincipal UserDetails user) {
        verifierOrganisation(service.trouverParId(id).organisationId(), user);
        service.supprimerSuivi(id);
    }

    private void verifierOrganisation(UUID organisationId, UserDetails user) {
        if (!estAdministrateur(user) && !UUID.fromString(user.getUsername()).equals(organisationId)) {
            throw new AccessDeniedException("Accès refusé à ce suivi.");
        }
    }

    private boolean estAdministrateur(UserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}