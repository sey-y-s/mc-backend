package odk.malicompetences.backend.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface NotificationDestinataireRepository
        extends JpaRepository<NotificationDestinataire, UUID> {

    Optional<NotificationDestinataire> findByIdAndUtilisateurId(
            UUID id,
            UUID utilisateurId
    );
}