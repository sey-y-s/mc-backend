package odk.malicompetences.backend.common.regle;

import java.time.Duration;
import java.time.LocalDateTime;

/** Règles de la mise en relation, sans dépendance technique. */
public final class ReglesMiseEnRelation {

    private ReglesMiseEnRelation() {}

    /**
     * Une nouvelle demande au même talent est possible si la dernière a été refusée il y a au moins {@code delai}.
     *
     * @param refuseLe date de réponse du dernier refus, ou null s'il n'y en a pas
     */
    public static boolean peutRedemanderApresRefus(LocalDateTime refuseLe, LocalDateTime maintenant, Duration delai) {
        return refuseLe == null || !maintenant.isBefore(refuseLe.plus(delai));
    }
}
