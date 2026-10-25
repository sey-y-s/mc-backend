package odk.malicompetences.backend.common.statut;

/**
 * Statut d une demande de validation.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum StatutDemandeValidation {
    EN_ATTENTE,
    EN_COURS,
    COMPLEMENT_REQUIS,
    VALIDEE,
    REJETEE,
    ANNULEE
}
