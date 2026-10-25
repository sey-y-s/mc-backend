package odk.malicompetences.backend.besoin;

import odk.malicompetences.backend.competence.NiveauCompetence;
import java.util.UUID;

public record BesoinCompetenceResponse(
        UUID id,
        UUID competenceId,
        String competenceNom,
        NiveauCompetence niveauRequis,
        Integer nombreRequis
) {}
