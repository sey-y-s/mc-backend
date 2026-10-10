package odk.malicompetences.backend.competence;

import org.springframework.stereotype.Component;

@Component
public class CitoyenCompetenceMapper {

    public CitoyenCompetenceResponse toResponse(
            CitoyenCompetence citoyenCompetence
    ) {
        return new CitoyenCompetenceResponse(
                citoyenCompetence.getId(),
                citoyenCompetence.getCompetence().getId(),
                citoyenCompetence.getCompetence().getNom(),
                citoyenCompetence.getNiveau(),
                citoyenCompetence.getEtat(),
                citoyenCompetence.getDateDeclaration()
        );
    }
}