package odk.malicompetences.backend.validation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DecisionValidationRepository extends JpaRepository<DecisionValidationEntity, UUID> {
    List<DecisionValidationEntity> findByDemandeIdOrderByDecideeLeAsc(UUID demandeId);
}
