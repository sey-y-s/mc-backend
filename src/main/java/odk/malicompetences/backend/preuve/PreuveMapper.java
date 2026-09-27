package odk.malicompetences.backend.preuve;

import org.springframework.stereotype.Component;

@Component
public class PreuveMapper {

    public PreuveResponse toResponse(Preuve preuve) {
        return new PreuveResponse(
                preuve.getId(),
                preuve.getCitoyenCompetence().getId(),
                preuve.getType(),
                preuve.getTitre(),
                preuve.getDescription(),
                preuve.getFichierUrl(),
                preuve.getDateDepot()
        );
    }
}