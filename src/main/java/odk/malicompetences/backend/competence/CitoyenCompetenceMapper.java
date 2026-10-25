package odk.malicompetences.backend.competence;

import org.springframework.stereotype.Component;
@Component
public class CitoyenCompetenceMapper {
    public CitoyenCompetenceResponse toResponse(CitoyenCompetence cc) {
        return new CitoyenCompetenceResponse(cc.getId(), cc.getCompetence().getId(), cc.getCompetence().getNom(),
                cc.getNiveauDeclare(), cc.getNiveauValide(), cc.getStatutValidation(), cc.getSituation(), cc.isDisponibilite(), cc.getDeclareeLe());
    }
}
