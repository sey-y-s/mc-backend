package odk.malicompetences.backend.common.regle;

import java.time.Duration;
import java.time.LocalDateTime;

/** Regles de la mise en relation reprises de l'ancien backend. */
public final class ReglesMiseEnRelation {

    /** Valeur de l'ancien backend ; a rendre configurable (app.regles.delai-apres-refus-jours). */
    public static final Duration DELAI_APRES_REFUS = Duration.ofDays(30);

    private ReglesMiseEnRelation() {}

    /** Une nouvelle demande est possible si la derniere a ete refusee il y a au moins {@code delai}. */
    public static boolean peutRedemanderApresRefus(LocalDateTime refuseLe, LocalDateTime maintenant, Duration delai) {
        return refuseLe == null || !maintenant.isBefore(refuseLe.plus(delai));
    }
}
