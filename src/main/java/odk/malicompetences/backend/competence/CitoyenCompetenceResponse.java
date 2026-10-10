package odk.malicompetences.backend.competence;

import java.time.LocalDateTime;
import java.util.UUID;

public record CitoyenCompetenceResponse(
        UUID id,
        UUID competenceId,
        String competenceNom,
        NiveauCompetence niveau,
        EtatCompetence etat,
        LocalDateTime dateDeclaration
) {
}