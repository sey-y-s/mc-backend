package odk.malicompetences.backend.auth;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptLimiterTest {

    @Test
    void bloqueApresCinqEchecsPuisSeDebloque() {
        long[] maintenant = {0L};
        LoginAttemptLimiter limiteur = new LoginAttemptLimiter(() -> maintenant[0]);

        for (int i = 0; i < LoginAttemptLimiter.MAX_ECHECS; i++) {
            limiteur.verifier("a@mail.com");
            limiteur.echec("a@mail.com");
        }
        assertThrows(ResponseStatusException.class, () -> limiteur.verifier("a@mail.com"));

        maintenant[0] += LoginAttemptLimiter.DUREE_MS + 1_000;
        assertDoesNotThrow(() -> limiteur.verifier("a@mail.com"));
    }

    @Test
    void uneConnexionReussieRemetLeCompteurAZero() {
        LoginAttemptLimiter limiteur = new LoginAttemptLimiter(() -> 0L);
        for (int i = 0; i < LoginAttemptLimiter.MAX_ECHECS - 1; i++) {
            limiteur.echec("b@mail.com");
        }
        limiteur.reussite("b@mail.com");
        limiteur.echec("b@mail.com");
        assertDoesNotThrow(() -> limiteur.verifier("b@mail.com"));
    }

    @Test
    void lesIdentifiantsSontIndependants() {
        LoginAttemptLimiter limiteur = new LoginAttemptLimiter(() -> 0L);
        for (int i = 0; i < LoginAttemptLimiter.MAX_ECHECS; i++) {
            limiteur.echec("c@mail.com");
        }
        assertDoesNotThrow(() -> limiteur.verifier("d@mail.com"));
    }
}
