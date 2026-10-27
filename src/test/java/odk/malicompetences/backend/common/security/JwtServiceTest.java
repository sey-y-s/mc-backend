package odk.malicompetences.backend.common.security;

import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private JwtService service() {
        return new JwtService(SECRET, 60_000L, new MockEnvironment());
    }

    private Utilisateur utilisateur() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UUID.randomUUID());
        utilisateur.setRole(Role.CITOYEN);
        return utilisateur;
    }

    @Test
    void unJetonValideEstAccepte() {
        JwtService service = service();
        Utilisateur utilisateur = utilisateur();
        String jeton = service.generateToken(utilisateur);

        assertTrue(service.isValid(jeton));
        assertEquals(utilisateur.getId(), service.extractUtilisateurId(jeton));
    }

    @Test
    void unJetonDeconnecteEstRefuse() {
        JwtService service = service();
        String jeton = service.generateToken(utilisateur());

        service.revoquer(jeton);

        assertFalse(service.isValid(jeton));
    }

    @Test
    void deconnecterUnJetonNeTouchePasLesAutres() {
        JwtService service = service();
        String premier = service.generateToken(utilisateur());
        String second = service.generateToken(utilisateur());

        service.revoquer(premier);

        assertFalse(service.isValid(premier));
        assertTrue(service.isValid(second));
    }

    @Test
    void unJetonFalsifieEstRefuse() {
        assertFalse(service().isValid("pas.un.jeton"));
    }
}
