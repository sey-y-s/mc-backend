package odk.malicompetences.backend.besoin;

import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/besoins")
public class BesoinCompetenceController {

    private final BesoinCompetenceService besoinCompetenceService;

    public BesoinCompetenceController(BesoinCompetenceService besoinCompetenceService) {
        this.besoinCompetenceService = besoinCompetenceService;
    }

    // Créer un besoin avec ses compétences associées
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BesoinResponse creerBesoin(
            @Valid @RequestBody BesoinRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        verifierOrganisation(request.organisationId(), user);
        return besoinCompetenceService.creerBesoin(request);
    }

    // Lister tous les besoins
    @GetMapping
    public List<BesoinResponse> listerTousLesBesoins(@AuthenticationPrincipal UserDetails user) {
        if (estAdministrateur(user)) {
            return besoinCompetenceService.listerTousLesBesoins();
        }
        return besoinCompetenceService.listerParOrganisation(UUID.fromString(user.getUsername()));
    }

    // Trouver un besoin par son ID
    @GetMapping("/{id}")
    public BesoinResponse trouverBesoinParId(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails user
    ) {
        BesoinResponse besoin = besoinCompetenceService.trouverBesoinParId(id);
        verifierOrganisation(besoin.organisationId(), user);
        return besoin;
    }

    @PutMapping("/{id}")
    public BesoinResponse modifierBesoin(
            @PathVariable UUID id,
            @Valid @RequestBody BesoinRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        verifierOrganisation(request.organisationId(), user);
        if (!estAdministrateur(user)) {
            BesoinResponse existant = besoinCompetenceService.trouverBesoinParId(id);
            verifierOrganisation(existant.organisationId(), user);
        }
        return besoinCompetenceService.modifierBesoin(id, request);
    }

    // Supprimer un besoin
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerBesoin(@PathVariable UUID id, @AuthenticationPrincipal UserDetails user) {
        BesoinResponse besoin = besoinCompetenceService.trouverBesoinParId(id);
        verifierOrganisation(besoin.organisationId(), user);
        besoinCompetenceService.supprimerBesoin(id);
    }

    private void verifierOrganisation(UUID organisationId, UserDetails user) {
        if (!estAdministrateur(user) && !UUID.fromString(user.getUsername()).equals(organisationId)) {
            throw new AccessDeniedException("Un compte organisation ne peut agir que sur ses propres besoins.");
        }
    }

    private boolean estAdministrateur(UserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}