package odk.malicompetences.backend.notification;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import java.util.UUID;

@RestController
@RequestMapping({"/api/notifications", "/api/v1/notifications"})
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Créer / Envoyer une notification
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void creerNotification(@Valid @RequestBody NotificationRequest request) {
        notificationService.creerEtEnvoyerNotification(request);
    }

    // Récupérer les notifications d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public List<NotificationResponse> recupererParUtilisateur(@PathVariable UUID utilisateurId, Authentication authentication) {
        verifierProprietaireOuAdmin(utilisateurId, authentication);
        return notificationService.recupererNotificationsParUtilisateur(utilisateurId);
    }

    // Récupérer les notifications non lues d'un utilisateur
    @GetMapping("/utilisateur/{utilisateurId}/non-lues")
    public List<NotificationResponse> recupererNonLues(@PathVariable UUID utilisateurId, Authentication authentication) {
        verifierProprietaireOuAdmin(utilisateurId, authentication);
        return notificationService.recupererNotificationsNonLues(utilisateurId);
    }

    private void verifierProprietaireOuAdmin(UUID utilisateurId, Authentication authentication) {
        boolean administrateur = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!administrateur && !authentication.getName().equals(utilisateurId.toString())) {
            throw new AccessDeniedException("Vous ne pouvez consulter que vos propres notifications.");
        }
    }

    // Marquer une notification comme lue
    @PatchMapping("/{notificationDestinataireId}/marquer-lue")
    public NotificationResponse marquerCommeLue(
            @PathVariable UUID notificationDestinataireId,
            Authentication authentication
    ) {
        boolean administrateur = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        UUID utilisateurId;
        try {
            utilisateurId = UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException("Utilisateur authentifié invalide");
        }
        return notificationService.marquerCommeLue(
                notificationDestinataireId, utilisateurId, administrateur
        );
    }
}
