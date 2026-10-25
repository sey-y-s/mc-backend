package odk.malicompetences.backend.common.otp;

import java.time.LocalDateTime;

/**
 * Etat d'un code de verification avant comparaison du hash. Compter TOUTE tentative (meme correcte
 * apres expiration) et incrementer {@code tentatives} avant de comparer, pour empecher l'essai en rafale.
 * Duree de validite et nombre maximal de tentatives : configuration (app.otp.*).
 */
public final class PolitiqueOtp {

    private PolitiqueOtp() {}

    public enum Etat { UTILISABLE, EXPIRE, DEJA_UTILISE, TROP_DE_TENTATIVES }

    public static Etat etat(LocalDateTime expireLe, LocalDateTime utiliseLe, int tentatives, int maxTentatives, LocalDateTime maintenant) {
        if (utiliseLe != null) return Etat.DEJA_UTILISE;
        if (!maintenant.isBefore(expireLe)) return Etat.EXPIRE;
        if (tentatives >= maxTentatives) return Etat.TROP_DE_TENTATIVES;
        return Etat.UTILISABLE;
    }
}
