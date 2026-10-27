package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;

/**
 * Crée le profil métier associé à un nouveau compte (citoyen, organisation…).
 * Ajouter un type de compte inscriptible = ajouter une implémentation, sans toucher à {@link InscriptionService}.
 */
public interface ProfilInitialiseur {

    /** Rôle pris en charge par cette implémentation. */
    Role role();

    /** Contrôle les données propres au profil (lève IllegalArgumentException si elles sont invalides). */
    void verifier(RegisterRequest request);

    /**
     * Exigences supplémentaires de l'inscription publique en autonomie (absentes de l'inscription assistée,
     * où l'agent peut compléter plus tard). Par défaut : aucune.
     */
    default void verifierInscriptionPublique(RegisterRequest request) {
    }

    /** Crée et enregistre le profil du compte déjà persisté. */
    void creer(Utilisateur compte, RegisterRequest request);
}
