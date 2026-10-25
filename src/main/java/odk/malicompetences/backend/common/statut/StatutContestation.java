package odk.malicompetences.backend.common.statut;

/**
 * Statut d une contestation.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum StatutContestation {
    OUVERTE,
    EN_EXAMEN,
    ACCEPTEE,
    REJETEE
}
