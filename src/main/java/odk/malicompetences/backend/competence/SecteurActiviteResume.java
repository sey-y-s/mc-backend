package odk.malicompetences.backend.competence;

import java.util.UUID;

/** Secteur d'activité tel qu'exposé en lecture publique. */
public record SecteurActiviteResume(UUID id, String nom, String description) {
    static SecteurActiviteResume de(SecteurActivite s) {
        return new SecteurActiviteResume(s.getId(), s.getNom(), s.getDescription());
    }
}
