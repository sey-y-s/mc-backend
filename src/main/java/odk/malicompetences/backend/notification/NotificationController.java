package odk.malicompetences.backend.notification;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/notifications")
public class NotificationController {
 private final NotificationService service;
 public NotificationController(NotificationService service){this.service=service;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')") public void creer(@Valid @RequestBody NotificationRequest request){service.creerEtEnvoyerNotification(request);}
 @GetMapping("/utilisateur/{utilisateurId}") public List<NotificationResponse> lister(@PathVariable UUID utilisateurId, Authentication auth){verifier(utilisateurId,auth);return service.recupererNotificationsParUtilisateur(utilisateurId);}
 @GetMapping("/utilisateur/{utilisateurId}/non-lues") public List<NotificationResponse> nonLues(@PathVariable UUID utilisateurId, Authentication auth){verifier(utilisateurId,auth);return service.recupererNotificationsNonLues(utilisateurId);}
 @PatchMapping("/{notificationId}/marquer-lue") public NotificationResponse lire(@PathVariable UUID notificationId, Authentication auth){ UUID user=uuid(auth); boolean admin=auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN")||a.getAuthority().equals("ROLE_SUPER_ADMIN")); return service.marquerCommeLue(notificationId,user,admin); }
 private void verifier(UUID id,Authentication a){boolean admin=a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")||x.getAuthority().equals("ROLE_SUPER_ADMIN"));if(!admin&&!uuid(a).equals(id))throw new AccessDeniedException("Vous ne pouvez consulter que vos notifications.");}
 private UUID uuid(Authentication a){try{return UUID.fromString(a.getName());}catch(Exception e){throw new AccessDeniedException("Utilisateur authentifié invalide");}}
}
