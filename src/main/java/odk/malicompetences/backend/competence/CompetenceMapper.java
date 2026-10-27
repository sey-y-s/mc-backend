package odk.malicompetences.backend.competence;

import org.springframework.stereotype.Component;

@Component
public class CompetenceMapper {

    public CompetenceResponse toResponse(Competence competence) {
        Metier metier = competence.getMetier();
        return new CompetenceResponse(
                competence.getId(),
                competence.getNom(),
                competence.getDescription(),
                metier != null ? metier.getNom() : null,
                metier != null && metier.getSecteurActivite() != null ? metier.getSecteurActivite().getNom() : null,
                competence.getParcoursAutorises()
        );
    }
}
