package odk.malicompetences.backend.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import odk.malicompetences.backend.utilisateur.Role;


public record RegisterRequest(
        @Size(max = 30, message = "Le téléphone ne doit pas dépasser 30 caractères")
        String telephone,

        @Size(max = 190, message = "L'email ne doit pas dépasser 190 caractères")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
        @JsonAlias("motDePasse")
        String password,

        @NotNull(message = "Le rôle est obligatoire")
        Role role,

        @NotNull(message = "L'acceptation des conditions est obligatoire")
        Boolean conditionsAcceptees
) {
}
