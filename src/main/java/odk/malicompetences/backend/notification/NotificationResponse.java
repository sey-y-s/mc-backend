package odk.malicompetences.backend.notification;
import java.time.LocalDateTime;
import java.util.UUID;
public record NotificationResponse(UUID id, String titre, String contenu, String typeEvenement, String typeRessource, UUID ressourceId, LocalDateTime creeLe, LocalDateTime lueLe) {}
