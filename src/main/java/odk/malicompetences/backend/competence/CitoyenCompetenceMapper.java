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
                citoyenCompetence.getDateDeclaration(),
                citoyenCompetence.getParcours(),
                ParcoursCompetence.preuvesDeposables(citoyenCompetence.getParcours()),
                citoyenCompetence.getPreuves() == null ? 0 : citoyenCompetence.getPreuves().size(),
                citoyenCompetence.getCompetence().getParcoursAutorises()
        );
    }
}
