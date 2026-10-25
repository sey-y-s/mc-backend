package odk.malicompetences.backend.common.statut;

/**
 * Competence exercee actuellement ou non.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum SituationCompetence {
    PRATIQUEE,
    NON_PRATIQUEE
}
