package odk.malicompetences.backend.common.statut;

/**
 * Decision historisee (immuable). Une correction cree une nouvelle decision.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum DecisionValidation {
    VALIDEE,
    REJETEE,
    COMPLEMENT_REQUIS,
    ANNULEE
}
