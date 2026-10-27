package odk.malicompetences.backend.auth;

import java.util.List;
import java.util.UUID;

/**
 * Réponse d'inscription : le compte est créé mais bloqué. Aucune session n'est ouverte
 * avant la vérification d'au moins un des contacts listés dans {@code canaux}.
 */
public record InscriptionResponse(
        UUID utilisateurId,
        boolean verificationRequise,
        List<CanalVerification> canaux
) {
}
