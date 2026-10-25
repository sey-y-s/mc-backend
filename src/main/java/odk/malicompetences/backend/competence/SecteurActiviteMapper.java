package odk.malicompetences.backend.competence;

import org.springframework.stereotype.Component;

@Component
public class SecteurActiviteMapper {
    public SecteurActiviteResponse toResponse(SecteurActivite secteur) {
        return new SecteurActiviteResponse(
                secteur.getId(), secteur.getCode(), secteur.getNom(),
                secteur.getDescription(), secteur.isActif()
        );
    }
}
