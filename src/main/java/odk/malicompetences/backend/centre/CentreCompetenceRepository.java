package odk.malicompetences.backend.centre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CentreCompetenceRepository extends JpaRepository<CentreCompetence, UUID> {

    List<CentreCompetence> findByCentre_Id(UUID centreId);

    Optional<CentreCompetence> findByCentre_IdAndCompetence_Id(UUID centreId, UUID competenceId);
}
