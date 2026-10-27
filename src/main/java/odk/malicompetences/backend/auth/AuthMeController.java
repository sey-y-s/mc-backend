package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthMeController {
    private final UtilisateurRepository repository;

    private odk.malicompetences.backend.utilisateur.RoleEffectif roleEffectif;

    @org.springframework.beans.factory.annotation.Autowired
    void setRoleEffectif(odk.malicompetences.backend.utilisateur.RoleEffectif roleEffectif) {
        this.roleEffectif = roleEffectif;
    }

    public AuthMeController(UtilisateurRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public SessionUtilisateur me(Authentication authentication) {
        UUID id;
        try {
            id = UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.security.access.AccessDeniedException("Identité de session invalide.");
        }
        Utilisateur user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        return new SessionUtilisateur(user.getId(), roleEffectif.de(user).name(), user.isActif());
    }

    public record SessionUtilisateur(UUID id, String role, boolean actif) {}
}
