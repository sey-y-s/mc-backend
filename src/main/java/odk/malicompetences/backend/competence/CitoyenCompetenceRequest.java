package odk.malicompetences.backend.competence;

import java.util.UUID;

public record CitoyenCompetenceRequest(
        UUID competenceId,
        NiveauCompetence niveau,
        /** FORMEL ou INFORMEL ; facultatif pour rester compatible avec les anciens clients. */
        ParcoursCompetence parcours
) {
}
