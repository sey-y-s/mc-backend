package odk.malicompetences.backend.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUtilisateur_IdOrderByCreeLeDesc(UUID utilisateurId);
    List<Notification> findByUtilisateur_IdAndLueLeIsNullOrderByCreeLeDesc(UUID utilisateurId);
    Optional<Notification> findByIdAndUtilisateur_Id(UUID id, UUID utilisateurId);
    boolean existsByUtilisateur_IdAndCleDeduplication(UUID utilisateurId, String cleDeduplication);
}
