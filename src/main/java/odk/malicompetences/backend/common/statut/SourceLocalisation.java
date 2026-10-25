package odk.malicompetences.backend.common.statut;

/**
 * Origine de la commune de reference. Le GPS ne la change jamais seul.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum SourceLocalisation {
    DECLARATION,
    GPS_CONFIRMEE,
    ADMINISTRATION
}
