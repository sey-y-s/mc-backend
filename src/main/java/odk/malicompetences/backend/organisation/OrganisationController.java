package odk.malicompetences.backend.organisation;

import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganisationResponse creer(
            @Valid @RequestBody OrganisationRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        verifierUtilisateur(request.utilisateurId(), user);
        if (!estAdministrateur(user)
                && request.statut() != null
                && request.statut() != StatutOrganisation.EN_ATTENTE) {
            throw new AccessDeniedException("Une organisation ne peut pas définir elle-même son statut.");
        }
        return service.creerOrganisation(request);
    }

    @GetMapping
    public List<OrganisationResponse> lister() {
        return service.listerToutes();
    }

    @GetMapping("/dashboard")
    public OrganisationDashboardResponse dashboard(@AuthenticationPrincipal UserDetails user) {
        return service.dashboard(UUID.fromString(user.getUsername()));
    }

    @GetMapping("/mine")
    public OrganisationResponse trouverPourUtilisateur(@AuthenticationPrincipal UserDetails user) {
        return service.trouverPourUtilisateur(UUID.fromString(user.getUsername()));
    }

    @GetMapping("/{id}")
    public OrganisationResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @PutMapping("/{id}")
    public OrganisationResponse modifier(
            @PathVariable UUID id,
            @Valid @RequestBody OrganisationRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        verifierUtilisateur(id, user);
        verifierUtilisateur(request.utilisateurId(), user);
        if (!estAdministrateur(user)) {
            OrganisationResponse existante = service.trouverParId(id);
            if (request.statut() != null && request.statut() != existante.statut()) {
                throw new AccessDeniedException("Seul un administrateur peut changer le statut d’une organisation.");
            }
        }
        return service.modifierOrganisation(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id, @AuthenticationPrincipal UserDetails user) {
        if (!estAdministrateur(user)) {
            throw new AccessDeniedException("Seul un administrateur peut supprimer une organisation.");
        }
        service.supprimerOrganisation(id);
    }

    private void verifierUtilisateur(UUID utilisateurId, UserDetails user) {
        if (!estAdministrateur(user) && !UUID.fromString(user.getUsername()).equals(utilisateurId)) {
            throw new AccessDeniedException("Vous ne pouvez modifier que votre propre organisation.");
        }
    }

    private boolean estAdministrateur(UserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}