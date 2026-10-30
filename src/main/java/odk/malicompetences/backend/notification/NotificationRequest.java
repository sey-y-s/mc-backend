package odk.malicompetences.backend.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record NotificationRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
        String titre,

        @NotBlank(message = "Le message est obligatoire")
        String message,

        @NotNull(message = "Le type de notification est obligatoire")
        TypeNotification type,

        @NotNull(message = "La liste des IDs des destinataires est obligatoire")
                List<UUID> destinatairesIds,

        /** Facultatif : identifiant de l'objet que l'alerte concerne. */
        UUID referenceId
) {
    public NotificationRequest(String titre, String message, TypeNotification type, List<UUID> destinatairesIds) {
        this(titre, message, type, destinatairesIds, null);
    }
}