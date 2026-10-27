package odk.malicompetences.backend.organisation;

import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrganisationMapper {

    private final OrganisationDocumentRepository documentRepository;

    public OrganisationMapper(OrganisationDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Organisation toEntity(OrganisationRequest request, Utilisateur utilisateur) {
        if (request == null) return null;

        Organisation organisation = new Organisation();
        organisation.setUtilisateur(utilisateur);
        organisation.setNom(request.nom());
        organisation.setDescription(request.description());

        if (request.statut() != null) {
            organisation.setStatut(request.statut());
        } else {
            organisation.setStatut(StatutOrganisation.EN_ATTENTE);
        }

        return organisation;
    }

    public void updateEntity(OrganisationRequest request, Organisation organisation) {
        if (request != null && organisation != null) {
            if (request.nom() != null) {
                organisation.setNom(request.nom());
            }
            if (request.description() != null) {
                organisation.setDescription(request.description());
            }
            if (request.statut() != null) {
                organisation.setStatut(request.statut());
            }
        }
    }

    public OrganisationResponse toResponse(Organisation org) {
        if (org == null) return null;

        UUID utilisateurId = org.getUtilisateur() != null ? org.getUtilisateur().getId() : null;
        String utilisateurEmail = org.getUtilisateur() != null ? org.getUtilisateur().getEmail() : null;
        String telephone = org.getUtilisateur() != null ? org.getUtilisateur().getTelephone() : null;
        var commune = org.getCommune();
        var region = commune != null ? commune.getRegion() : null;

        List<OrganisationDocument> documents = org.getId() == null ? List.of()
                : documentRepository.findByOrganisationIdOrderByDateDepotDesc(org.getId());
        Set<TypeDocumentOrganisation> fournis = documents.stream()
                .filter(d -> d.getStatut() != StatutDocumentOrganisation.REJETE)
                .map(OrganisationDocument::getTypeDocument)
                .collect(Collectors.toSet());
        TypeOrganisation type = org.getTypeOrganisation();
        List<TypeDocumentOrganisation> obligatoires = type == null ? List.of() : type.getObligatoires();
        List<TypeDocumentOrganisation> conseilles = type == null ? List.of() : type.getConseillees();
        List<TypeDocumentOrganisation> manquants = obligatoires.stream().filter(d -> !fournis.contains(d)).toList();

        return new OrganisationResponse(
                org.getId(),
                utilisateurId,
                utilisateurEmail,
                org.getNom(),
                org.getDescription(),
                org.getStatut(),
                org.getMotifStatut(),
                type,
                type == null ? null : type.getLibelle(),
                org.getSigle(),
                org.getFormeJuridique(),
                org.getSecteurActivite(),
                org.getDateCreation(),
                org.getEffectif(),
                org.getNumeroRccm(),
                org.getNif(),
                org.getNumeroInps(),
                org.getNumeroRecepisse(),
                org.getNumeroAccordCadre(),
                org.getReferenceActe(),
                telephone,
                org.getAdresse(),
                commune != null ? commune.getId() : null,
                commune != null ? commune.getNom() : null,
                region != null ? region.getId() : null,
                region != null ? region.getNom() : null,
                org.getSiteWeb(),
                org.getResponsableNom(),
                org.getResponsableFonction(),
                org.getResponsableTelephone(),
                obligatoires,
                conseilles,
                manquants,
                documents.size(),
                type != null && manquants.isEmpty()
        );
    }
}
