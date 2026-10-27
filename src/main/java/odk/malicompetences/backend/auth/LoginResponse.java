package odk.malicompetences.backend.auth;

/**
 * Réponse de connexion : un jeton d'accès de courte durée et un jeton de renouvellement
 * (à usage unique) qui permet d'obtenir un nouvel accès sans ressaisir le mot de passe.
 */
public record LoginResponse(
        String token,
        String refreshToken,
        Long expiresIn
) {
    /** Durée de vie du jeton d'accès en millisecondes ({@code expiresIn}). */
    public LoginResponse(String token, String refreshToken) {
        this(token, refreshToken, null);
    }

    /** Réponse sans jeton de renouvellement (le contrôleur l'ajoute ensuite). */
    public LoginResponse(String token) {
        this(token, null, null);
    }
}
