package odk.malicompetences.backend.common.statut;

/**
 * Statut de validation d une competence de citoyen. Une declaration n est jamais une validation.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum StatutValidationCompetence {
    NON_VERIFIEE,
    EN_COURS,
    VALIDEE,
    REJETEE,
    A_REVALIDER
}
