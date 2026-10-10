package odk.malicompetences.backend.experience;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ExperienceResponse(
        UUID id,
        UUID citoyenId,
        String citoyenNomComplet,
        String titre,
        String description,
        String entreprise,
        LocalDate dateDebut,
        LocalDate dateFin,
        Boolean enCours,
        Set<UUID> citoyenCompetenceIds
) {}