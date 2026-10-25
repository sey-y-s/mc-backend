package odk.malicompetences.backend.competence;

import org.springframework.stereotype.Component;

@Component
public class MetierMapper {
    public MetierResponse toResponse(Metier metier) {
        SecteurActivite secteur = metier.getSecteurActivite();
        return new MetierResponse(
                metier.getId(), metier.getCode(), metier.getNom(), metier.getDescription(),
                metier.isActif(), secteur.getId(), secteur.getCode(), secteur.getNom(),
                metier.getCompetences().stream()
                        .map(c -> new MetierResponse.CompetenceResume(c.getId(), c.getCode(), c.getNom()))
                        .sorted(java.util.Comparator.comparing(MetierResponse.CompetenceResume::nom))
                        .toList()
        );
    }
}
