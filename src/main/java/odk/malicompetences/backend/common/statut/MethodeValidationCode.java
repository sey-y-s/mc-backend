package odk.malicompetences.backend.common.statut;

/**
 * Codes de methode_validation. Les methodes sont alternatives ; aucune n exige un document.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum MethodeValidationCode {
    DOCUMENT,
    TEST_NUMERIQUE,
    EVALUATION_CENTRE,
    PORTFOLIO,
    MAITRE_ARTISAN
}
