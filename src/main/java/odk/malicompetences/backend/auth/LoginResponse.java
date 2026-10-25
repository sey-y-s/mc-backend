package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.statut.StatutCompte;

import java.util.Set;
import java.util.UUID;

public record LoginResponse(
        String token,
        String accessToken,
        String refreshToken,
        Long expiresIn,
        UtilisateurSession utilisateur
) {
    public LoginResponse(String token) {
        this(token, token, null, 86400000L, null);
    }

    public record UtilisateurSession(
            UUID id,
            String telephone,
            String email,
            StatutCompte statut,
            Set<String> roles
    ) {}
}
