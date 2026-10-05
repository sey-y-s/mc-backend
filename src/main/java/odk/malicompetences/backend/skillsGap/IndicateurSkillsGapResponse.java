package odk.malicompetences.backend.skillsGap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record IndicateurSkillsGapResponse(
        UUID id,
        UUID regionId,
        String regionNom,
        UUID competenceId,
        String competenceNom,
        LocalDate periodeDebut,
        LocalDate periodeFin,
        LocalDateTime dateCalcul,
        Integer demandeEstimee,
        Integer offreDisponible,
        Integer ecart
) {}