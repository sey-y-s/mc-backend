package odk.malicompetences.backend.preuve;

import odk.malicompetences.backend.competence.CitoyenCompetence;
import org.springframework.stereotype.Component;

@Component
public class PreuveMapper {

    public Preuve toEntity(PreuveRequest request, CitoyenCompetence citoyenCompetence) {
        Preuve preuve = new Preuve();
        preuve.setCitoyenCompetence(citoyenCompetence);
        preuve.setType(request.type());
        preuve.setTitre(request.titre());
        preuve.setReferenceDocument(request.referenceDocument());
        preuve.setDescription(request.description());
        preuve.setVisibleProfil(request.visibleProfil());
        return preuve;
    }

    public void patch(PreuveRequest request, Preuve preuve) {
        preuve.setTitre(request.titre());
        preuve.setDescription(request.description());
        preuve.setVisibleProfil(request.visibleProfil());
    }

    public PreuveResponse toResponse(Preuve preuve) {
        return new PreuveResponse(
                preuve.getId(),
                preuve.getCitoyenCompetence().getId(),
                preuve.getType(),
                preuve.getTitre(),
                preuve.getReferenceDocument(),
                preuve.getDescription(),
                preuve.getStatut(),
                preuve.isVisibleProfil(),
                preuve.getDeposeeLe(),
                preuve.getCleStockagePrive() != null
        );
    }
}
