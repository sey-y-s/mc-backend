package odk.malicompetences.backend.competence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompetenceRepository
        extends JpaRepository<Competence, UUID> {
    boolean existsByMetierId(UUID id);
}