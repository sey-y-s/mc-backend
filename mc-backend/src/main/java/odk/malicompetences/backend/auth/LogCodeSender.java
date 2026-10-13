package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Implémentation de DÉVELOPPEMENT : écrit le code dans la console du backend.
 * Active par défaut tant qu'aucune passerelle SMS/email n'est branchée. Ne pas utiliser en production.
 */
@Component
@ConditionalOnProperty(prefix = "app.reset", name = "sender", havingValue = "log", matchIfMissing = true)
public class LogCodeSender implements CodeSender {

    private static final Logger log = LoggerFactory.getLogger(LogCodeSender.class);

    @Override
    public void envoyer(Utilisateur utilisateur, String code) {
        log.warn("[DEV UNIQUEMENT] Code de réinitialisation pour {} : {}", utilisateur.getTelephone(), code);
    }
}
