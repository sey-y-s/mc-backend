package odk.malicompetences.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import odk.malicompetences.backend.utilisateur.Role;

public record RegisterRequest(
        @NotBlank(message = "Le téléphone est obligatoire")
        @Size(max = 30)
        String telephone,
        @Email(message = "L'email est invalide")
        @Size(max = 255)
        String email,
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 100)
        String password,
        Role role
) {}
