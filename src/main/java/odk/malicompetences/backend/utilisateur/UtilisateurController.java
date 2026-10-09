package odk.malicompetences.backend.utilisateur;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/utilisateurs")
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

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}")
    public UtilisateurResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
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