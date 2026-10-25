package odk.malicompetences.backend.dev;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("dev")
public class DevOtpCache {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public void enregistrer(String destinataire, String code) {
        if (destinataire != null && code != null) {
            cache.put(destinataire.trim().toLowerCase(), code);
        }
    }

    public Optional<String> getDernierOtp(String destinataire) {
        if (destinataire == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cache.get(destinataire.trim().toLowerCase()));
    }
}

