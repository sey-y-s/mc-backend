package odk.malicompetences.backend.competence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CitoyenCompetenceRepository
        extends JpaRepository<CitoyenCompetence, UUID> {
}