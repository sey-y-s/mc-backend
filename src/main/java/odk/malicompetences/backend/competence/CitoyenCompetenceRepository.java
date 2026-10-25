package odk.malicompetences.backend.competence;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CitoyenCompetenceRepository
        extends JpaRepository<CitoyenCompetence, UUID> {

    List<CitoyenCompetence> findByCitoyenId(UUID citoyenId);

    boolean existsByCompetence_Id(UUID competenceId);

    boolean existsByCitoyenIdAndCompetenceId(
            UUID citoyenId,
            UUID competenceId
    );

    java.util.Optional<CitoyenCompetence> findByCitoyenIdAndCompetenceId(
            UUID citoyenId, UUID competenceId
    );
}