package odk.malicompetences.backend.common.otp;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OtpTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 22, 10, 0);

    @Test
    void codeAsixChiffres() {
        for (int i = 0; i < 200; i++) {
            assertTrue(GenerateurCodeOtp.generer().matches("\\d{6}"));
        }
    }

    @Test
    void etatsDuCode() {
        assertEquals(PolitiqueOtp.Etat.UTILISABLE, PolitiqueOtp.etat(NOW.plusMinutes(5), null, 0, 5, NOW));
        assertEquals(PolitiqueOtp.Etat.EXPIRE, PolitiqueOtp.etat(NOW, null, 0, 5, NOW));
        assertEquals(PolitiqueOtp.Etat.DEJA_UTILISE, PolitiqueOtp.etat(NOW.plusMinutes(5), NOW.minusMinutes(1), 0, 5, NOW));
        assertEquals(PolitiqueOtp.Etat.TROP_DE_TENTATIVES, PolitiqueOtp.etat(NOW.plusMinutes(5), null, 5, 5, NOW));
    }
}
