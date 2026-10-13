package odk.malicompetences.backend.competence;

import org.springframework.stereotype.Component;

@Component
public class CompetenceMapper {

    public CompetenceResponse toResponse(Competence competence) {
        return new CompetenceResponse(
                competence.getId(),
                competence.getNom(),
                competence.getDescription()
        );
    }
}