package odk.malicompetences.backend.opportunite;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OpportuniteMapper {

    private final CandidatureOpportuniteRepository candidatureRepository;

    public OpportuniteMapper(CandidatureOpportuniteRepository candidatureRepository) {
        this.candidatureRepository = candidatureRepository;
    }


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
        if (request == null || opportunite == null) return;
        if (categorie != null) opportunite.setCategorie(categorie);
        opportunite.setTitre(request.titre());
        opportunite.setDescription(request.description());
        opportunite.setType(request.type());
        opportunite.setDateExpiration(request.dateExpiration());

        if (request.statut() != opportunite.getStatut()) {
            if (request.statut() == StatutOpportunite.PUBLIEE) {
                opportunite.setDatePublication(LocalDateTime.now());
            }
            opportunite.setStatut(request.statut());
        }
    }

    public OpportuniteResponse toResponse(Opportunite op) {
        if (op == null) return null;
        UUID categorieId = op.getCategorie() != null ? op.getCategorie().getId() : null;
        String categorieNom = op.getCategorie() != null ? op.getCategorie().getNom() : null;

        var region = op.getRegion();
        var commune = op.getCommune();
        String localite = commune != null
                ? (region != null ? region.getNom() + " · " : "") + commune.getNom()
                : region != null ? "Région de " + region.getNom() : "Tout le Mali";
        long candidatures = op.getId() == null || candidatureRepository == null ? 0 : candidatureRepository.countByOpportuniteId(op.getId());

        return new OpportuniteResponse(
                op.getId(), categorieId, categorieNom, op.getTitre(), op.getDescription(),
                op.getStatut(), op.getType(), op.getDatePublication(), op.getDateExpiration(),
                region != null ? region.getId() : null, region != null ? region.getNom() : null,
                commune != null ? commune.getId() : null, commune != null ? commune.getNom() : null,
                localite, candidatures
        );
    }
}
