package odk.malicompetences.backend.organisation;

import java.util.List;

/**
 * Nature juridique de l'organisation (droit malien / OHADA) et pièces qui l'autorisent à exercer.
 * {@code obligatoires} : pièces à fournir pour que le dossier soit complet ; {@code conseillees} : utiles à la vérification.
 */
public enum TypeOrganisation {
    ENTREPRISE("Entreprise (SA, SARL, SAS…)",
            List.of(TypeDocumentOrganisation.RCCM, TypeDocumentOrganisation.NIF),
            List.of(TypeDocumentOrganisation.STATUTS, TypeDocumentOrganisation.ATTESTATION_INPS, TypeDocumentOrganisation.QUITUS_FISCAL)),
    ENTREPRISE_INDIVIDUELLE("Entreprise individuelle",
            List.of(TypeDocumentOrganisation.RCCM, TypeDocumentOrganisation.NIF),
            List.of(TypeDocumentOrganisation.ATTESTATION_INPS)),
    GIE("Groupement d'intérêt économique (GIE)",
            List.of(TypeDocumentOrganisation.RCCM, TypeDocumentOrganisation.STATUTS),
            List.of(TypeDocumentOrganisation.NIF)),
    COOPERATIVE("Société coopérative",
            List.of(TypeDocumentOrganisation.RCCM, TypeDocumentOrganisation.STATUTS),
            List.of(TypeDocumentOrganisation.NIF)),
    ASSOCIATION("Association",
            List.of(TypeDocumentOrganisation.RECEPISSE_ASSOCIATION, TypeDocumentOrganisation.STATUTS),
            List.of(TypeDocumentOrganisation.NIF)),
    ONG("Organisation non gouvernementale (ONG)",
            List.of(TypeDocumentOrganisation.RECEPISSE_ASSOCIATION, TypeDocumentOrganisation.ACCORD_CADRE_ONG, TypeDocumentOrganisation.STATUTS),
            List.of(TypeDocumentOrganisation.NIF)),
    ETABLISSEMENT_PUBLIC("Établissement ou service public",
            List.of(TypeDocumentOrganisation.ACTE_CREATION),
            List.of()),
    COLLECTIVITE_TERRITORIALE("Collectivité territoriale",
            List.of(TypeDocumentOrganisation.ACTE_CREATION),
            List.of()),
    ORGANISATION_INTERNATIONALE("Organisation internationale ou projet de coopération",
            List.of(TypeDocumentOrganisation.AGREMENT),
            List.of());

    private final String libelle;
    private final List<TypeDocumentOrganisation> obligatoires;
    private final List<TypeDocumentOrganisation> conseillees;

    TypeOrganisation(String libelle, List<TypeDocumentOrganisation> obligatoires, List<TypeDocumentOrganisation> conseillees) {
        this.libelle = libelle;
        this.obligatoires = obligatoires;
        this.conseillees = conseillees;
    }

    public String getLibelle() { return libelle; }
    public List<TypeDocumentOrganisation> getObligatoires() { return obligatoires; }
    public List<TypeDocumentOrganisation> getConseillees() { return conseillees; }
}
