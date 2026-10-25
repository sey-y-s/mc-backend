package odk.malicompetences.backend.common.statut;

/**
 * Raison d un OTP.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum UsageCodeVerification {
    INSCRIPTION,
    RECUPERATION_MOT_DE_PASSE,
    CHANGEMENT_CONTACT
}
