package odk.malicompetences.backend.common.otp;

import odk.malicompetences.backend.common.statut.CanalVerification;
import odk.malicompetences.backend.common.statut.UsageCodeVerification;

/**
 * Port d'envoi d'un code OTP. Implementation actuelle : {@link ConsoleEnvoiCodeVerification} (simulation).
 * Pour un vrai service : creer une implementation SMS/email, l'activer avec app.otp.sender=sms (par exemple).
 */
public interface EnvoiCodeVerification {
    void envoyer(CanalVerification canal, String destinataire, UsageCodeVerification usage, String codeEnClair);
}
