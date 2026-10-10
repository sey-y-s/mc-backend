package odk.malicompetences.backend.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationDestinataireRepository
        extends JpaRepository<NotificationDestinataire, UUID> {
    long countByUtilisateurIdAndLuFalse(UUID utilisateurId);
    List<NotificationDestinataire> findByUtilisateurIdOrderByDateReceptionDesc(UUID utilisateurId);
    List<NotificationDestinataire> findByUtilisateurIdAndLuFalseOrderByDateReceptionDesc(UUID utilisateurId);
    Optional<NotificationDestinataire> findByIdAndUtilisateurId(UUID id, UUID utilisateurId);
}