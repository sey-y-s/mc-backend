package odk.malicompetences.backend.common.statut;

/**
 * Statut d un compte (utilisateur.statut). EN_ATTENTE tant que l OTP n est pas valide.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum StatutCompte {
    EN_ATTENTE,
    ACTIF,
    SUSPENDU,
    FERME
}
