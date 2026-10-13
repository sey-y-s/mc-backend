package odk.malicompetences.backend.skillsGap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IndicateurSkillsGapRepository
        extends JpaRepository<IndicateurSkillsGap, UUID> {
}