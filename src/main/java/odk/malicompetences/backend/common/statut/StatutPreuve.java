package odk.malicompetences.backend.common.statut;

/**
 * Statut d une preuve.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum StatutPreuve {
    EN_ATTENTE,
    VERIFIEE,
    REJETEE,
    ARCHIVEE
}
