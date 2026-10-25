package odk.malicompetences.backend.common.statut;

/**
 * Type de preuve ou de realisation (remplace preuve.TypePreuve).
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum TypePreuveCitoyen {
    DIPLOME,
    ATTESTATION,
    PHOTO,
    VIDEO,
    PORTFOLIO,
    AUTRE
}
