package odk.malicompetences.backend.common.statut;

/**
 * Objet d une contestation.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum TypeObjetContestation {
    DECISION_VALIDATION,
    DONNEE_PROFIL,
    AUTRE
}
