package odk.malicompetences.backend.common.statut;

/**
 * Un domaine de centre porte soit un secteur, soit un metier.
 * Les constantes doivent rester IDENTIQUES a la contrainte CHECK correspondante de
 * V1__schema_initial.sql (verifie par MigrationEnumsAlignmentTest).
 */
public enum TypeDomaineCentre {
    SECTEUR,
    METIER
}
