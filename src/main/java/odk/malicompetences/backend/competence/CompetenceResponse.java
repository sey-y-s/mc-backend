package odk.malicompetences.backend.competence;

import java.util.List;
import java.util.UUID;

public record CompetenceResponse(
        UUID id,
        String nom,
        String description,
        String metierNom,
        String secteurNom,
        /** Secteurs par lesquels la compétence peut s'acquérir (FORMEL, INFORMEL). */
        List<ParcoursCompetence> parcoursPossibles
) {
}
