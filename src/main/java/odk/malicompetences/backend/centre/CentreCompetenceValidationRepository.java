package odk.malicompetences.backend.centre;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CentreCompetenceValidationRepository extends JpaRepository<CentreCompetenceValidation, UUID> {
    List<CentreCompetenceValidation> findByCentre_IdAndCompetence_IdAndActiveTrue(UUID centreId, UUID competenceId);
}
