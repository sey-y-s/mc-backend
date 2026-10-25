package odk.malicompetences.backend.common.statut;

/**
 * Statut d un centre.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum StatutCentre {
    EN_ATTENTE,
    ACTIF,
    SUSPENDU
}
