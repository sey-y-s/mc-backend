package odk.malicompetences.backend.experience;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ExperienceRequest(
        UUID citoyenId,
        @NotBlank @Size(max=180) String titre,
        String description,
        @Size(max=180) String entreprise,
        LocalDate dateDebut,
        LocalDate dateFin,
        Boolean enCours,
        Set<UUID> citoyenCompetenceIds
) {}
