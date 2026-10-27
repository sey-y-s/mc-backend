package odk.malicompetences.backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/** Bloque 15 minutes un identifiant après 5 échecs de connexion consécutifs (protection contre les essais répétés). */
@Component
public class LoginAttemptLimiter {

    static final int MAX_ECHECS = 5;
    static final long DUREE_MS = 15 * 60 * 1000L;

    private record Etat(int echecs, long debutFenetre, long bloqueJusqua) { }

    private final Map<String, Etat> etats = new ConcurrentHashMap<>();
    private final LongSupplier horloge;

    public LoginAttemptLimiter() {
        this(System::currentTimeMillis);
    }

    LoginAttemptLimiter(LongSupplier horloge) {
        this.horloge = horloge;
    }

    /** Lève une erreur 429 si l'identifiant est actuellement bloqué. */
    public void verifier(String cle) {
        Etat etat = etats.get(cle);
        long maintenant = horloge.getAsLong();
        if (etat != null && etat.bloqueJusqua() > maintenant) {
            long minutes = Math.max(1, (etat.bloqueJusqua() - maintenant + 59_999) / 60_000);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Trop de tentatives. Réessayez dans " + minutes + " minute(s).");
        }
    }

    public void echec(String cle) {
        long maintenant = horloge.getAsLong();
        etats.compute(cle, (k, etat) -> {
            if (etat == null || maintenant - etat.debutFenetre() > DUREE_MS) {
                return new Etat(1, maintenant, 0);
            }
            int echecs = etat.echecs() + 1;
            return echecs >= MAX_ECHECS
                    ? new Etat(echecs, etat.debutFenetre(), maintenant + DUREE_MS)
                    : new Etat(echecs, etat.debutFenetre(), 0);
        });
        if (etats.size() > 10_000) {
            etats.values().removeIf(e -> maintenant - e.debutFenetre() > DUREE_MS);
        }
    }

    public void reussite(String cle) {
        etats.remove(cle);
    }
}
