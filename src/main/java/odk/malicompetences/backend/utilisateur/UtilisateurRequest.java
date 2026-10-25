package odk.malicompetences.backend.utilisateur;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UtilisateurRequest(
        @Size(max = 30, message = "Le téléphone ne doit pas dépasser 30 caractères")
        String telephone,

        @Email(message = "Format d'email invalide")
        @Size(max = 190, message = "L'email ne doit pas dépasser 190 caractères")
        String email,
        
        String password,
        
        Role role,
        
        Boolean actif,
        
        Set<Role> roles
) {
    public UtilisateurRequest(String telephone, String email, String password, Role role, Boolean actif) {
        this(telephone, email, password, role, actif, role == null ? Set.of() : Set.of(role));
    }
}
