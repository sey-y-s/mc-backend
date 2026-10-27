package odk.malicompetences.backend.organisation;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OrganisationResponse(
        UUID id,
        UUID utilisateurId,
        String utilisateurEmail,
        String nom,
        String description,
        StatutOrganisation statut,
        String motifStatut,
        // Identité juridique
        TypeOrganisation typeOrganisation,
        String typeOrganisationLibelle,
        String sigle,
        String formeJuridique,
        String secteurActivite,
        LocalDate dateCreation,
        Integer effectif,
        String numeroRccm,
        String nif,
        String numeroInps,
        String numeroRecepisse,
        String numeroAccordCadre,
        String referenceActe,
        // Coordonnées
        String telephone,
        String adresse,
        UUID communeId,
        String communeNom,
        UUID regionId,
        String regionNom,
        String siteWeb,
        String responsableNom,
        String responsableFonction,
        String responsableTelephone,
        // Dossier
        List<TypeDocumentOrganisation> documentsObligatoires,
        List<TypeDocumentOrganisation> documentsConseilles,
        List<TypeDocumentOrganisation> documentsManquants,
        int nombreDocuments,
        boolean dossierComplet
) {}
