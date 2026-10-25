package odk.malicompetences.backend.competence;

import odk.malicompetences.backend.common.statut.SituationCompetence;
import odk.malicompetences.backend.common.statut.StatutValidationCompetence;
import java.time.LocalDateTime;
import java.util.UUID;

public record CitoyenCompetenceResponse(
        UUID id, UUID competenceId, String competenceNom,
        NiveauCompetence niveauDeclare, NiveauCompetence niveauValide,
        StatutValidationCompetence statutValidation, SituationCompetence situation,
        boolean disponibilite, LocalDateTime declareeLe
) {}
