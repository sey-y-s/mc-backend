package odk.malicompetences.backend.common.regle;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Decide s'il faut proposer au citoyen de confirmer un changement de commune.
 *
 * <p>Le GPS sert uniquement a SUGGERER : la position est recue, convertie en commune, puis oubliee
 * (jamais stockee, jamais journalisee, pas dans les messages d'erreur). La commune de reference
 * (localisation_profil) ne change qu'apres confirmation explicite du citoyen.
 */
public final class ReglesLocalisation {

    private ReglesLocalisation() {}

    public enum Decision {
        PROPOSER,
        RIEN_A_FAIRE,
        GPS_NON_CONSENTI,
        COMMUNE_INCONNUE,
        DEJA_EN_ATTENTE,
        DELAI_NON_ECOULE
    }

    /**
     * @param communeDetectee  commune deduite de la position recue (null si hors referentiel ou indisponible)
     * @param derniereReponse  date de reponse a la derniere demande (acceptee ou refusee), ou null
     * @param delai            delai minimal avant une nouvelle suggestion (configuration)
     */
    public static Decision evaluer(
            boolean gpsConsenti,
            boolean demandeEnAttente,
            UUID communeReference,
            UUID communeDetectee,
            LocalDateTime derniereReponse,
            LocalDateTime maintenant,
            Duration delai
    ) {
        if (!gpsConsenti) return Decision.GPS_NON_CONSENTI;
        if (communeDetectee == null) return Decision.COMMUNE_INCONNUE;
        if (communeDetectee.equals(communeReference)) return Decision.RIEN_A_FAIRE;
        if (demandeEnAttente) return Decision.DEJA_EN_ATTENTE;
        if (derniereReponse != null && maintenant.isBefore(derniereReponse.plus(delai))) return Decision.DELAI_NON_ECOULE;
        return Decision.PROPOSER;
    }
}
