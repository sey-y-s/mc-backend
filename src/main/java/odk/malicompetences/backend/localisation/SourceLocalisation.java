package odk.malicompetences.backend.localisation;

/** Origine de la commune de référence d'un citoyen. */
public enum SourceLocalisation {
    /** Saisie par le citoyen (inscription, profil, déménagement signalé). */
    DECLARATION,
    /** Suggestion issue d'une détection GPS ponctuelle, confirmée par le citoyen. */
    GPS_CONFIRMEE,
    /** Corrigée par l'administration (par exemple après une contestation). */
    ADMINISTRATION
}
