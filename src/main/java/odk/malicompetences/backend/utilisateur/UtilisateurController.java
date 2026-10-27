package odk.malicompetences.backend.utilisateur;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/v1/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurResponse creer(@Valid @RequestBody UtilisateurRequest request) {
        return service.creerUtilisateur(request);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public List<UtilisateurResponse> lister() {
        return service.listerTous();
    }

    @PreAuthorize("#id.toString() == authentication.name or hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}")
    public UtilisateurResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @PreAuthorize("hasRole('CITOYEN') and #id.toString() == authentication.name")
    @PutMapping("/{id}/contact")
    public UtilisateurResponse modifierMesCoordonnees(
            @PathVariable UUID id,
            @Valid @RequestBody UtilisateurContactRequest request,
            Authentication authentication
    ) {
        if (!id.toString().equals(authentication.getName())) {
            throw new AccessDeniedException("Vous ne pouvez modifier que vos propres coordonnées");
        }
        return service.modifierCoordonnees(id, request);
    }

    @PreAuthorize("hasRole('CITOYEN')")
    @GetMapping("/me/preferences")
    public PreferencesUtilisateurRequest lireMesPreferences(Authentication authentication) {
        return service.lireMesPreferences(UUID.fromString(authentication.getName()));
    }

    @PreAuthorize("hasRole('CITOYEN')")
    @PutMapping("/me/preferences")
    public PreferencesUtilisateurRequest modifierMesPreferences(
            @Valid @RequestBody PreferencesUtilisateurRequest request,
            Authentication authentication
    ) {
        return service.modifierMesPreferences(
                UUID.fromString(authentication.getName()), request
        );
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public UtilisateurResponse modifier(@PathVariable UUID id, @Valid @RequestBody UtilisateurRequest request) {
        return service.modifierUtilisateur(id, request);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerUtilisateur(id);
    }
}