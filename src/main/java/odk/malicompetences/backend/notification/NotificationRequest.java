package odk.malicompetences.backend.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record NotificationRequest(
 @NotBlank @Size(max=180) String titre,
 @NotBlank @Size(max=1000) String contenu,
 @NotNull TypeNotification type,
 @NotEmpty List<UUID> destinatairesIds,
 @Size(max=80) String typeRessource,
 UUID ressourceId,
 @Size(max=190) String cleDeduplication
) {}
