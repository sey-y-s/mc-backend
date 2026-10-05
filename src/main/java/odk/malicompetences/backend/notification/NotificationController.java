package odk.malicompetences.backend.notification;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Créer / Envoyer une notification
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void creerNotification(@Valid @RequestBody NotificationRequest request) {
        notificationService.creerEtEnvoyerNotification(request);
    }

    // Récupérer les notifications d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public List<NotificationResponse> recupererParUtilisateur(@PathVariable UUID utilisateurId) {
        return notificationService.recupererNotificationsParUtilisateur(utilisateurId);
    }

    // Récupérer les notifications non lues d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/non-lues")
    public List<NotificationResponse> recupererNonLues(@PathVariable UUID utilisateurId) {
        return notificationService.recupererNotificationsNonLues(utilisateurId);
    }

    // Marquer une notification comme lue
    @PatchMapping("/{notificationDestinataireId}/marquer-lue")
    public NotificationResponse marquerCommeLue(@PathVariable UUID notificationDestinataireId) {
        return notificationService.marquerCommeLue(notificationDestinataireId);
    }
}