package odk.malicompetences.backend.experience;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ExperienceMapper {

    public ExperienceResponse toResponse(Experience experience) {
        return new ExperienceResponse(
                experience.getId(),
                experience.getTitre(),
                experience.getDescription(),
                experience.getEntreprise(),
                experience.getDateDebut(),
                experience.getDateFin(),
                experience.getEnCours(),
                experience.getCompetences()
                        .stream()
                        .map(CitoyenCompetence::getId)
                        .collect(Collectors.toSet())
        );
    }
}