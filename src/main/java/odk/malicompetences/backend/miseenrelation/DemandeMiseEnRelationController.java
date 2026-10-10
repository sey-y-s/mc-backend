package odk.malicompetences.backend.miseenrelation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/demandes-mise-en-relation")
public class DemandeMiseEnRelationController {

    private final DemandeMiseEnRelationService service;

    public DemandeMiseEnRelationController(DemandeMiseEnRelationService service) {
        this.service = service;
    }

    // Créer
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeMiseEnRelationResponse creer(
            @Valid @RequestBody DemandeMiseEnRelationRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        boolean organisation = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ORGANISATION"));
        if (!isAdmin(user) && (!organisation || !UUID.fromString(user.getUsername()).equals(request.demandeurId()))) {
            throw new AccessDeniedException("Vous ne pouvez créer une demande qu’en votre nom.");
        }
        return service.creerDemande(request);
    }

    // Lister tout
    @GetMapping
    public List<DemandeMiseEnRelationResponse> lister(@AuthenticationPrincipal UserDetails user) {
        if (isAdmin(user)) {
            return service.listerToutes();
        }
        return service.listerParDemandeur(UUID.fromString(user.getUsername()));
    }

    // Trouver par ID
    @GetMapping("/{id}")
    public DemandeMiseEnRelationResponse trouverParId(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails user
    ) {
        DemandeMiseEnRelationResponse demande = service.trouverParId(id);
        verifierAcces(demande, user);
        return demande;
    }

    // Modifier (ex: changer le statut en ACCEPTEE ou REFUSEE)
    @PutMapping("/{id}")
    public DemandeMiseEnRelationResponse modifier(
            @PathVariable UUID id,
            @Valid @RequestBody DemandeMiseEnRelationRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        verifierAcces(service.trouverParId(id), user);
        return service.modifierDemande(id, request);
    }

    // Supprimer
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id, @AuthenticationPrincipal UserDetails user) {
        verifierAcces(service.trouverParId(id), user);
        service.supprimerDemande(id);
    }

    private void verifierAcces(DemandeMiseEnRelationResponse demande, UserDetails user) {
        UUID userId = UUID.fromString(user.getUsername());
        boolean concerne = userId.equals(demande.demandeurId()) || userId.equals(demande.destinataireId());
        if (!isAdmin(user) && !concerne) {
            throw new AccessDeniedException("Accès refusé à cette demande.");
        }
    }

    private boolean isAdmin(UserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}