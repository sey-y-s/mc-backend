package odk.malicompetences.backend.common.statut;

/**
 * Statut d une habilitation d evaluateur. L expiration se juge aussi sur date_fin.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum StatutHabilitation {
    EN_ATTENTE,
    ACTIVE,
    SUSPENDUE,
    REVOQUEE
}
