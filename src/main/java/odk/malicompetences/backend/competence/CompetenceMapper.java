package odk.malicompetences.backend.competence;

import org.springframework.stereotype.Component;

@Component
public class CompetenceMapper {
    public CompetenceResponse toResponse(Competence competence) {
        return new CompetenceResponse(
                competence.getId(), competence.getCode(), competence.getNom(),
                competence.getDescription(), competence.isActif(),
                competence.getMetiers().stream()
                        .map(m -> new CompetenceResponse.MetierResume(
                                m.getId(), m.getCode(), m.getNom(),
                                m.getSecteurActivite().getId(), m.getSecteurActivite().getNom()))
                        .sorted(java.util.Comparator.comparing(CompetenceResponse.MetierResume::nom))
                        .toList()
        );
    }
}
