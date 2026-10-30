package odk.malicompetences.backend.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID destinataireId, // ID de la liaison NotificationDestinataire (utile pour marquer comme lu)
        String titre,
        String message,
        TypeNotification type,
        Boolean lu,
        LocalDateTime dateReception,
        LocalDateTime dateLecture,
                LocalDateTime dateCreation,
        UUID referenceId
) {}