package odk.malicompetences.backend.contestation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ContestationRepository extends JpaRepository<Contestation, UUID> {

    List<Contestation> findByCitoyen_IdOrderByDateCreationDesc(UUID citoyenId);

    List<Contestation> findByStatutInOrderByDateCreationAsc(Collection<StatutContestation> statuts);

    List<Contestation> findAllByOrderByDateCreationDesc();

    boolean existsByValidation_IdAndStatutIn(UUID validationId, Collection<StatutContestation> statuts);
}
