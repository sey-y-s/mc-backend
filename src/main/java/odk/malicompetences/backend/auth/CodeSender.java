package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.utilisateur.Utilisateur;

/**
 * Envoi du code de réinitialisation à l'utilisateur (SMS, email...).
 * Pour la production, créer une implémentation SMS/email et activer app.reset.sender=sms (par exemple).
 */
public interface CodeSender {
    void envoyer(Utilisateur utilisateur, String code);
}
