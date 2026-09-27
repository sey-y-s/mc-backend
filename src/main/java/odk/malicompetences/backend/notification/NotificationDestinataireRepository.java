package odk.malicompetences.backend.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationDestinataireRepository
        extends JpaRepository<NotificationDestinataire, UUID> {
}