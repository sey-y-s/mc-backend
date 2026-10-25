package odk.malicompetences.backend.common.statut;

/**
 * Statut d un evaluateur.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum StatutEvaluateur {
    EN_ATTENTE,
    ACTIF,
    SUSPENDU
}
