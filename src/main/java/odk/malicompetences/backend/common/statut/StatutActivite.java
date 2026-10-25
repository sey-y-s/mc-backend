package odk.malicompetences.backend.common.statut;

/**
 * Situation professionnelle du citoyen.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum StatutActivite {
    NON_RENSEIGNE,
    EN_ACTIVITE,
    SANS_ACTIVITE,
    EN_FORMATION,
    ETUDIANT,
    EN_RECONVERSION,
    RETRAITE,
    AUTRE
}
