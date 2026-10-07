package odk.malicompetences.backend.opportunite;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID; // <--- Correction 1 : Import de UUID manquant

@Component
public class OpportuniteMapper {

    public Opportunite toEntity(OpportuniteRequest request, CategorieOpportunite categorie) {
        if (request == null) return null;

        Opportunite op = new Opportunite();
        op.setCategorie(categorie);
        op.setTitre(request.titre());
        op.setDescription(request.description());
        op.setStatut(request.statut());
        op.setType(request.type());
        op.setDateExpiration(request.dateExpiration());

        if (request.statut() == StatutOpportunite.PUBLIEE) {
            op.setDatePublication(LocalDateTime.now());
        }

        return op;
    }

    public void updateEntity(OpportuniteRequest request, Opportunite opportunite, CategorieOpportunite categorie) {
        if (request != null && opportunite != null) {
            if (categorie != null) {
                opportunite.setCategorie(categorie);
            }
            opportunite.setTitre(request.titre());
            opportunite.setDescription(request.description());
            opportunite.setType(request.type());
            opportunite.setDateExpiration(request.dateExpiration());

            if (request.statut() != opportunite.getStatut()) {
                if (request.statut() == StatutOpportunite.PUBLIEE && opportunite.getDatePublication() == null) {
                    opportunite.setDatePublication(LocalDateTime.now());
                }
                opportunite.setStatut(request.statut());
            }
        }
    }

    public OpportuniteResponse toResponse(Opportunite op) {
        if (op == null) return null;

        UUID catId = op.getCategorie() != null ? op.getCategorie().getId() : null;
        String catNom = op.getCategorie() != null ? op.getCategorie().getNom() : null;

        return new OpportuniteResponse(
                op.getId(),
                catId,
                catNom,
                op.getTitre(),
                op.getDescription(),
                op.getStatut(),
                op.getType(),
                op.getDatePublication(),
                op.getDateExpiration()
        );
    }
}