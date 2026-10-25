package odk.malicompetences.backend.miseenrelation;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.talent.SuiviBesoinTalent;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DemandeMiseEnRelationMapper {

    public DemandeMiseEnRelation toEntity(
            DemandeMiseEnRelationRequest request,
            Utilisateur demandeur,
            Citoyen destinataire,
            SuiviBesoinTalent suivi
    ) {
        DemandeMiseEnRelation demande = new DemandeMiseEnRelation();
        demande.setDemandeur(demandeur);
        demande.setDestinataire(destinataire);
        demande.setSuiviBesoinTalent(suivi);
        demande.setStatut(StatutContact.EN_ATTENTE);
        demande.setEnAttenteCle(suivi == null ? destinataire.getId() : suivi.getId());
        demande.setMessage(request.message().trim());
        return demande;
    }

    public DemandeMiseEnRelationResponse toResponse(DemandeMiseEnRelation demande) {
        if (demande == null) return null;

        Utilisateur demandeur = demande.getDemandeur();
        Citoyen destinataire = demande.getDestinataire();
        Organisation organisation = demandeur == null ? null : demandeur.getOrganisation();
        Citoyen citoyenDemandeur = demandeur == null ? null : demandeur.getCitoyen();

        UUID demandeurId = null;
        String demandeurNom = "Utilisateur";
        if (organisation != null) {
            demandeurId = organisation.getId();
            demandeurNom = organisation.getNom();
        } else if (citoyenDemandeur != null) {
            demandeurId = citoyenDemandeur.getIdentifiantPublic();
            demandeurNom = demande.getStatut() == StatutContact.ACCEPTEE
                    ? citoyenDemandeur.getPrenom() + " " + citoyenDemandeur.getNom()
                    : pseudo(citoyenDemandeur.getIdentifiantPublic());
        }

        UUID destinataireId = destinataire == null ? null : destinataire.getIdentifiantPublic();
        String destinataireNom = destinataire == null ? null
                : demande.getStatut() == StatutContact.ACCEPTEE
                ? destinataire.getPrenom() + " " + destinataire.getNom()
                : pseudo(destinataireId);

        UUID suiviId = demande.getSuiviBesoinTalent() == null
                ? null : demande.getSuiviBesoinTalent().getId();

        return new DemandeMiseEnRelationResponse(
                demande.getId(), demandeurId, demandeurNom,
                destinataireId, destinataireNom, suiviId,
                demande.getStatut(), demande.getMessage(),
                demande.getDateDemande(), demande.getDateReponse()
        );
    }

    private String pseudo(UUID identifiantPublic) {
        return identifiantPublic == null ? "Profil talent"
                : "Profil-" + identifiantPublic.toString().substring(0, 8);
    }
}
