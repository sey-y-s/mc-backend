package odk.malicompetences.backend.common.regle;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Décide s'il faut proposer au citoyen de confirmer un changement de commune.
 *
 * <p>La position GPS sert uniquement à SUGGÉRER : elle est reçue, convertie en commune, puis oubliée (jamais stockée).
 * La commune de référence ne change qu'après confirmation explicite du citoyen.
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
     * @param communeDetectee commune déduite de la position (null si hors référentiel)
     * @param derniereReponse date de réponse à la dernière demande refusée, ou null
     * @param delai           délai minimal avant une nouvelle suggestion
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
