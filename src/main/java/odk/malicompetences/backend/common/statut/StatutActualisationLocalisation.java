package odk.malicompetences.backend.common.statut;

/**
 * Statut d une demande d actualisation de commune.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum StatutActualisationLocalisation {
    EN_ATTENTE,
    ACCEPTEE,
    REFUSEE,
    EXPIREE
}
