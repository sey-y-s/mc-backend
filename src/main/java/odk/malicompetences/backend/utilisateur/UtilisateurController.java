package odk.malicompetences.backend.utilisateur;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurResponse creer(@Valid @RequestBody UtilisateurRequest request) {
        return service.creerUtilisateur(request);
    }

    @GetMapping
    public List<UtilisateurResponse> lister() {
        return service.listerTous();
    }

    @GetMapping("/{id}")
    public UtilisateurResponse trouverParId(@PathVariable UUID id) {
        return service.trouverParId(id);
    }

    @PutMapping("/{id}")
    public UtilisateurResponse modifier(@PathVariable UUID id, @Valid @RequestBody UtilisateurRequest request) {
        return service.modifierUtilisateur(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimerUtilisateur(id);
    }
}