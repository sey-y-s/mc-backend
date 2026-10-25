package odk.malicompetences.backend.common.statut;

/**
 * Statut d un justificatif d organisation.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum StatutJustificatif {
    RENSEIGNE,
    VERIFIE,
    REJETE,
    EXPIRE
}
