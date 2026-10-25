package odk.malicompetences.backend.organisation;


import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrganisationMapper {

    public Organisation toEntity(OrganisationRequest request, Utilisateur utilisateur) {
        if (request == null) return null;

        Organisation organisation = new Organisation();
        organisation.setUtilisateur(utilisateur);
        organisation.setNom(request.nom());
        organisation.setDescription(request.description());
        organisation.setNumeroRccm(normaliserRccm(request.numeroRccm()));

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
            if (request.numeroRccm() != null) {
                organisation.setNumeroRccm(normaliserRccm(request.numeroRccm()));
            }
        }
    }

    private String normaliserRccm(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur.trim();
    }

    public OrganisationResponse toResponse(Organisation org) {
        if (org == null) return null;

        UUID utilisateurId = org.getUtilisateur() != null ? org.getUtilisateur().getId() : null;
        String utilisateurEmail = org.getUtilisateur() != null ? org.getUtilisateur().getEmail() : null;

        return new OrganisationResponse(
                org.getId(),
                utilisateurId,
                utilisateurEmail,
                org.getNom(),
                org.getDescription(),
                org.getNumeroRccm(),
                org.getStatut(),
                org.getMotifStatut()
        );
    }
}