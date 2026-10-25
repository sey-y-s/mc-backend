package odk.malicompetences.backend.utilisateur;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UtilisateurResponse(
        UUID id,
        String telephone,
        String email,
        boolean actif,
        Role role,
        Set<Role> roles,
        LocalDateTime dateCreation
) {
    public UtilisateurResponse(UUID id, String telephone, String email, boolean actif, Role role, LocalDateTime dateCreation) {
        this(id, telephone, email, actif, role, role == null ? Set.of() : Set.of(role), dateCreation);
    }
}
