package odk.malicompetences.backend.dev;

import odk.malicompetences.backend.common.otp.EnvoiCodeVerification;
import odk.malicompetences.backend.common.statut.CanalVerification;
import odk.malicompetences.backend.common.statut.UsageCodeVerification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@Primary
public class DevEnvoiCodeVerification implements EnvoiCodeVerification {

    private static final Logger log = LoggerFactory.getLogger(DevEnvoiCodeVerification.class);

    private final DevOtpCache devOtpCache;

    public DevEnvoiCodeVerification(DevOtpCache devOtpCache) {
        this.devOtpCache = devOtpCache;
    }

    @Override
    public void envoyer(CanalVerification canal, String destinataire, UsageCodeVerification usage, String codeEnClair) {
        log.warn("[DEV OTP] {} -> {} ({}) : {}", canal, destinataire, usage, codeEnClair);
        devOtpCache.enregistrer(destinataire, codeEnClair);
    }
}

