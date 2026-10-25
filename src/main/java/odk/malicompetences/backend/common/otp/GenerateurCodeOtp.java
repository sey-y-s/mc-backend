package odk.malicompetences.backend.common.otp;

import java.security.SecureRandom;

/** Generateur de code OTP a 6 chiffres (SecureRandom). Ne stocker QUE le hash du code. */
public final class GenerateurCodeOtp {

    private static final SecureRandom RANDOM = new SecureRandom();

    private GenerateurCodeOtp() {}

    public static String generer() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
