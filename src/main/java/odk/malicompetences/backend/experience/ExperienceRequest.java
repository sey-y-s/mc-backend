package odk.malicompetences.backend.experience;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExperienceRequest(
        @NotNull(message = "L'ID du citoyen est obligatoire")
        UUID citoyenId,

        @NotBlank(message = "Le titre est obligatoire")
        String titre,

        String description,

        String entreprise,

        @NotNull(message = "La date de début est obligatoire")
        LocalDate dateDebut,

        LocalDate dateFin,

        @NotNull(message = "Le statut 'en cours' est obligatoire")
        Boolean enCours,

        Set<UUID> citoyenCompetenceIds
) {}