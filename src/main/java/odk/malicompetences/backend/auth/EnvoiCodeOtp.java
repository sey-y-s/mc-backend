package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.utilisateur.Utilisateur;

/**
 * Envoi du code de vérification (SMS pour le téléphone, email pour l'adresse).
 * Pour la production : créer une implémentation SMS/email et activer app.otp.sender=sms (par exemple).
 */
public interface EnvoiCodeOtp {
    void envoyer(Utilisateur utilisateur, CanalVerification canal, String code);
}
