package odk.malicompetences.backend.organisation;

import odk.malicompetences.backend.territoire.CommuneRepository;

/** Contrôle et application de l'identité juridique d'une organisation. */
public final class OrganisationRegles {

    private OrganisationRegles() {
    }

    private static String propre(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    private static void exiger(String valeur, String message) {
        if (valeur == null || valeur.isBlank()) throw new IllegalArgumentException(message);
    }

    /** Numéros légaux exigés selon la nature juridique. */
    public static void verifier(OrganisationIdentite d) {
        if (d.typeOrganisation() == null) {
            throw new IllegalArgumentException("Le type d'organisation est obligatoire.");
        }
        exiger(d.responsableNom(), "Le nom du responsable légal est obligatoire.");
        if (d.communeId() == null) {
            throw new IllegalArgumentException("La commune du siège est obligatoire.");
        }
        switch (d.typeOrganisation()) {
            case ENTREPRISE, ENTREPRISE_INDIVIDUELLE -> {
                exiger(d.numeroRccm(), "Le numéro RCCM est obligatoire pour une entreprise.");
                exiger(d.nif(), "Le NIF est obligatoire pour une entreprise.");
            }
            case GIE, COOPERATIVE -> exiger(d.numeroRccm(), "Le numéro d'immatriculation (RCCM) est obligatoire.");
            case ASSOCIATION -> exiger(d.numeroRecepisse(), "Le numéro du récépissé de déclaration est obligatoire pour une association.");
            case ONG -> {
                exiger(d.numeroRecepisse(), "Le numéro du récépissé de déclaration est obligatoire pour une ONG.");
                exiger(d.numeroAccordCadre(), "Le numéro de l'accord-cadre est obligatoire pour une ONG.");
            }
            case ETABLISSEMENT_PUBLIC, COLLECTIVITE_TERRITORIALE ->
                    exiger(d.referenceActe(), "La référence de l'acte de création est obligatoire.");
            case ORGANISATION_INTERNATIONALE ->
                    exiger(d.referenceActe(), "La référence de l'accord de siège ou de l'agrément est obligatoire.");
        }
        if (d.siteWeb() != null && !d.siteWeb().isBlank() && !d.siteWeb().trim().matches("(?i)^(https?://)?[\\w.-]+\\.[a-z]{2,}.*$")) {
            throw new IllegalArgumentException("L'adresse du site web est invalide.");
        }
    }

    /** Recopie l'identité dans l'entité (les champs vides sont effacés). */
    public static void appliquer(OrganisationIdentite d, Organisation o, CommuneRepository communes) {
        o.setTypeOrganisation(d.typeOrganisation());
        o.setSigle(propre(d.sigle()));
        o.setFormeJuridique(propre(d.formeJuridique()));
        o.setSecteurActivite(propre(d.secteurActivite()));
        o.setDateCreation(d.dateCreation());
        o.setEffectif(d.effectif());
        o.setNumeroRccm(propre(d.numeroRccm()) == null ? null : d.numeroRccm().trim().toUpperCase(java.util.Locale.ROOT));
        o.setNif(propre(d.nif()) == null ? null : d.nif().trim().toUpperCase(java.util.Locale.ROOT));
        o.setNumeroInps(propre(d.numeroInps()));
        o.setNumeroRecepisse(propre(d.numeroRecepisse()));
        o.setNumeroAccordCadre(propre(d.numeroAccordCadre()));
        o.setReferenceActe(propre(d.referenceActe()));
        o.setAdresse(propre(d.adresse()));
        o.setSiteWeb(propre(d.siteWeb()));
        o.setResponsableNom(propre(d.responsableNom()));
        o.setResponsableFonction(propre(d.responsableFonction()));
        o.setResponsableTelephone(propre(d.responsableTelephone()));
        o.setCommune(d.communeId() == null ? null : communes.findById(d.communeId())
                .orElseThrow(() -> new IllegalArgumentException("Commune introuvable.")));
    }
}
