package odk.malicompetences.backend.auth;

/** Le mot de passe est bon mais ni le téléphone ni l'email n'ont été vérifiés : la session n'est pas ouverte. */
public class CompteNonVerifieException extends RuntimeException {
    public CompteNonVerifieException() {
        super("Votre compte n'est pas encore vérifié. Saisissez le code reçu par SMS ou par email.");
    }
}
