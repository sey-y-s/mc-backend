package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.validation.NumeroTelephone;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;

import java.util.Locale;
import java.util.Optional;

/** Retrouve un compte à partir d'un identifiant saisi : numéro de téléphone ou email. */
final class AuthIdentifiant {

    private AuthIdentifiant() { }

    static Optional<Utilisateur> chercher(UtilisateurRepository repository, String saisie) {
        String valeur = saisie == null ? "" : saisie.trim();
        String identifiant;
        try {
            identifiant = NumeroTelephone.ressembleANumero(valeur)
                    ? NumeroTelephone.normaliser(valeur)
                    : valeur.toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        Optional<Utilisateur> parTelephone = repository.findByTelephone(identifiant);
        return parTelephone.isPresent() ? parTelephone : repository.findByEmail(identifiant);
    }
}
