package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Simulation : le code est affiché dans la console du serveur, en attendant une passerelle SMS/email. */
@Component
@ConditionalOnProperty(prefix = "app.otp", name = "sender", havingValue = "log", matchIfMissing = true)
public class ConsoleEnvoiCodeOtp implements EnvoiCodeOtp {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEnvoiCodeOtp.class);

    @Override
    public void envoyer(Utilisateur utilisateur, CanalVerification canal, String code) {
        String destinataire = canal == CanalVerification.EMAIL ? utilisateur.getEmail() : utilisateur.getTelephone();
        log.warn("[DEV] Code de vérification {} pour {} : {}", canal, destinataire, code);
    }
}
