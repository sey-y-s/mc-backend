package odk.malicompetences.backend.organisation;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrganisationDocumentResponse(
        UUID id,
        UUID organisationId,
        TypeDocumentOrganisation typeDocument,
        String typeDocumentLibelle,
        String nomFichier,
        String typeContenu,
        Long tailleOctets,
        StatutDocumentOrganisation statut,
        String commentaire,
        LocalDateTime dateDepot
) {
    static OrganisationDocumentResponse from(OrganisationDocument d) {
        return new OrganisationDocumentResponse(d.getId(), d.getOrganisation().getId(), d.getTypeDocument(),
                d.getTypeDocument().getLibelle(), d.getNomFichier(), d.getTypeContenu(), d.getTailleOctets(),
                d.getStatut(), d.getCommentaire(), d.getDateDepot());
    }
}
