package odk.malicompetences.backend.competence;

import java.util.UUID;

/** Métier tel qu'exposé en lecture publique, avec son secteur. */
public record MetierResume(UUID id, UUID secteurId, String secteurNom, String nom, String description) {
    static MetierResume de(Metier m) {
        return new MetierResume(m.getId(), m.getSecteurActivite().getId(), m.getSecteurActivite().getNom(),
                m.getNom(), m.getDescription());
    }
}
