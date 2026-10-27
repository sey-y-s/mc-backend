package odk.malicompetences.backend.organisation;

/** Pièces officielles maliennes qui attestent l'existence légale et le droit d'exercer d'une organisation. */
public enum TypeDocumentOrganisation {
    RCCM("Immatriculation au RCCM (Registre du commerce et du crédit mobilier)"),
    NIF("Numéro d'identification fiscale (NIF)"),
    STATUTS("Statuts signés"),
    ATTESTATION_INPS("Attestation d'immatriculation à l'INPS"),
    QUITUS_FISCAL("Attestation de régularité fiscale (quitus)"),
    RECEPISSE_ASSOCIATION("Récépissé de déclaration de l'association"),
    ACCORD_CADRE_ONG("Accord-cadre ONG signé avec l'État"),
    ACTE_CREATION("Acte de création (loi, décret, arrêté ou délibération)"),
    AGREMENT("Agrément ou autorisation d'exercer"),
    PIECE_IDENTITE_RESPONSABLE("Pièce d'identité du responsable"),
    AUTRE("Autre document");

    private final String libelle;

    TypeDocumentOrganisation(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
