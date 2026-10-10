package odk.malicompetences.backend.opportunite;

import org.springframework.stereotype.Component;

@Component
public class CategorieOpportuniteMapper {

    public CategorieOpportunite toEntity(CategorieOpportuniteRequest request) {
        if (request == null) return null;
        CategorieOpportunite cat = new CategorieOpportunite();
        cat.setNom(request.nom());
        cat.setDescription(request.description());
        return cat;
    }

    public CategorieOpportuniteResponse toResponse(CategorieOpportunite cat) {
        if (cat == null) return null;
        return new CategorieOpportuniteResponse(
                cat.getId(),
                cat.getNom(),
                cat.getDescription()
        );
    }
}