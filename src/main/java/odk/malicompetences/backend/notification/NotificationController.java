package odk.malicompetences.backend.notification;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public void creerNotification(
            @Valid @RequestBody NotificationRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        if (!estAdministrateur(user)) {
            throw new AccessDeniedException("Seul un administrateur peut envoyer une notification.");
        }
        notificationService.creerEtEnvoyerNotification(request);
    }

    // Récupérer les notifications d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public List<NotificationResponse> recupererParUtilisateur(
            @PathVariable UUID utilisateurId,
            @AuthenticationPrincipal UserDetails user
    ) {
        verifierUtilisateur(utilisateurId, user);
        return notificationService.recupererNotificationsParUtilisateur(utilisateurId);
    }

    // Récupérer les notifications non lues d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/non-lues")
    public List<NotificationResponse> recupererNonLues(
            @PathVariable UUID utilisateurId,
            @AuthenticationPrincipal UserDetails user
    ) {
        verifierUtilisateur(utilisateurId, user);
        return notificationService.recupererNotificationsNonLues(utilisateurId);
    }

    // Marquer une notification comme lue
    @PatchMapping("/{notificationDestinataireId}/marquer-lue")
    public NotificationResponse marquerCommeLue(
            @PathVariable UUID notificationDestinataireId,
            @AuthenticationPrincipal UserDetails user
    ) {
        return notificationService.marquerCommeLue(
                notificationDestinataireId, UUID.fromString(user.getUsername()));
    }

    private void verifierUtilisateur(UUID utilisateurId, UserDetails user) {
        if (!UUID.fromString(user.getUsername()).equals(utilisateurId) && !estAdministrateur(user)) {
            throw new AccessDeniedException("Vous ne pouvez consulter que vos notifications.");
        }
    }

    private boolean estAdministrateur(UserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}