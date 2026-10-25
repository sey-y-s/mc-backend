package odk.malicompetences.backend.common.otp;

import odk.malicompetences.backend.common.statut.CanalVerification;
import odk.malicompetences.backend.common.statut.UsageCodeVerification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * SIMULATION : affiche le code dans la console du serveur. Desactive dans le profil "prod" et "dev" :
 * en dev, DevEnvoiCodeVerification prend le relais avec cache memoire pour les tests d'API.
 */
@Component
@Profile("!prod & !dev")
@ConditionalOnProperty(prefix = "app.otp", name = "sender", havingValue = "console", matchIfMissing = true)
public class ConsoleEnvoiCodeVerification implements EnvoiCodeVerification {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEnvoiCodeVerification.class);

    @Override
    public void envoyer(CanalVerification canal, String destinataire, UsageCodeVerification usage, String codeEnClair) {
        log.warn("[SIMULATION OTP] {} -> {} ({}) : {}", canal, destinataire, usage, codeEnClair);
    }
}
