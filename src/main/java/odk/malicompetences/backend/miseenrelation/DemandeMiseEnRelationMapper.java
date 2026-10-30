package odk.malicompetences.backend.miseenrelation;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.talent.SuiviBesoinTalent;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID; // <--- C'est cet import qu'il manquait pour UUID

@Component
public class DemandeMiseEnRelationMapper {

    public DemandeMiseEnRelation toEntity(DemandeMiseEnRelationRequest request, Utilisateur demandeur, Citoyen destinataire, SuiviBesoinTalent SuiviBesoinTalent) {
        if (request == null) {
            return null;
        }

        DemandeMiseEnRelation demande = new DemandeMiseEnRelation();
        demande.setDemandeur(demandeur);
        demande.setDestinataire(destinataire);
        demande.setSuiviBesoinTalent(SuiviBesoinTalent);
        demande.setStatut(request.statut());
        demande.setMessage(request.message());
        demande.setDateDemande(LocalDateTime.now());

        if (request.statut() != StatutContact.EN_ATTENTE) {
            demande.setDateReponse(LocalDateTime.now());
        }

        return demande;
    }

    public void updateEntityFromRequest(DemandeMiseEnRelationRequest request, DemandeMiseEnRelation demande) {
        if (request != null && demande != null) {
            if (request.statut() != null && !demande.getStatut().equals(request.statut())) {
                demande.setStatut(request.statut());
                demande.setDateReponse(LocalDateTime.now());
            }
            if (request.message() != null) {
                demande.setMessage(request.message());
            }
        }
    }

        public DemandeMiseEnRelationResponse toResponse(DemandeMiseEnRelation demande) {
        return toResponse(demande, null);
    }

    /**
     * Vue d'un participant : une fois la demande acceptée, il reçoit les coordonnées de l'autre personne
     * (jamais avant, et jamais un tiers).
     */
    public DemandeMiseEnRelationResponse toResponse(DemandeMiseEnRelation demande, UUID viewerId) {
        if (demande == null) {
            return null;
        }
        UUID demandeurId = demande.getDemandeur() != null ? demande.getDemandeur().getId() : null;
        String demandeurNomComplet = nomDuDemandeur(demande.getDemandeur());

        UUID destinataireId = demande.getDestinataire() != null ? demande.getDestinataire().getId() : null;
        String destinataireNomComplet = demande.getDestinataire() != null ? demande.getDestinataire().getPrenom() + " " + demande.getDestinataire().getNom() : null;

        UUID suiviId = demande.getSuiviBesoinTalent() != null ? demande.getSuiviBesoinTalent().getId() : null;

        return new DemandeMiseEnRelationResponse(
                demande.getId(),
                demandeurId,
                demandeurNomComplet,
                destinataireId,
                destinataireNomComplet,
                suiviId,
                demande.getStatut(),
                demande.getMessage(),
                                demande.getDateDemande(),
                demande.getDateReponse(),
                contactPour(demande, viewerId)
        );
    }

    private static String nomDuDemandeur(Utilisateur demandeur) {
        if (demandeur == null) return null;
        if (demandeur.getCitoyen() != null) {
            return demandeur.getCitoyen().getPrenom() + " " + demandeur.getCitoyen().getNom();
        }
        if (demandeur.getOrganisation() != null) {
            return demandeur.getOrganisation().getNom();
        }
        return demandeur.getEmail();
    }

    private static DemandeMiseEnRelationResponse.Contact contactPour(DemandeMiseEnRelation demande, UUID viewerId) {
        if (viewerId == null || demande.getStatut() != StatutContact.ACCEPTEE
                || demande.getDemandeur() == null || demande.getDestinataire() == null) {
            return null;
        }
        Utilisateur autre;
        if (viewerId.equals(demande.getDemandeur().getId())) {
            autre = demande.getDestinataire().getUtilisateur();
        } else if (viewerId.equals(demande.getDestinataire().getId())) {
            autre = demande.getDemandeur();
        } else {
            return null;
        }
        return autre == null ? null : new DemandeMiseEnRelationResponse.Contact(autre.getTelephone(), autre.getEmail());
    }
}