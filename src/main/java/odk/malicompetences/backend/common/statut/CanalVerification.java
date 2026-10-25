package odk.malicompetences.backend.common.statut;

/**
 * Canal d envoi d un OTP.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum CanalVerification {
    TELEPHONE,
    EMAIL
}
