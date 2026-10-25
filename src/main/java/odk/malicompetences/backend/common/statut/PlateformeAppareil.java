package odk.malicompetences.backend.common.statut;

/**
 * Plateforme d un appareil de notification.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum PlateformeAppareil {
    ANDROID,
    IOS,
    WEB
}
