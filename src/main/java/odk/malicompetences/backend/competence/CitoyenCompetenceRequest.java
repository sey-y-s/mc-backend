package odk.malicompetences.backend.competence;

import com.fasterxml.jackson.annotation.JsonAlias;
import odk.malicompetences.backend.common.statut.SituationCompetence;
import java.util.UUID;

public record CitoyenCompetenceRequest(
        UUID competenceId,
        @JsonAlias("niveau") NiveauCompetence niveauDeclare,
        SituationCompetence situation,
        Boolean disponibilite
) {}
