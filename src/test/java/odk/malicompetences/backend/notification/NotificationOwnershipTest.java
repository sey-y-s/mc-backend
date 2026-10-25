package odk.malicompetences.backend.notification;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional; import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*; import static org.mockito.Mockito.*;
class NotificationOwnershipTest {
 @Test void utilisateurNePeutPasLireNotificationDUnAutre(){
  NotificationRepository repo=mock(NotificationRepository.class); UUID id=UUID.randomUUID(), userId=UUID.randomUUID();
  when(repo.findByIdAndUtilisateur_Id(id,userId)).thenReturn(Optional.empty());
  NotificationService service=new NotificationService(repo,mock(UtilisateurRepository.class),new NotificationMapper());
  assertThrows(ResourceNotFoundException.class,()->service.marquerCommeLue(id,userId,false));
  verify(repo,never()).save(any());
 }
 @Test void deduplicationEstVerifieeParDestinataire(){
  NotificationRepository repo=mock(NotificationRepository.class); UtilisateurRepository users=mock(UtilisateurRepository.class); UUID userId=UUID.randomUUID();
  Utilisateur u=new Utilisateur();u.setId(userId); when(users.findById(userId)).thenReturn(Optional.of(u)); when(repo.existsByUtilisateur_IdAndCleDeduplication(userId,"evt-1")).thenReturn(true);
  NotificationService service=new NotificationService(repo,users,new NotificationMapper());
  service.creerEtEnvoyerNotification(new NotificationRequest("Titre","Contenu",TypeNotification.SYSTEME,java.util.List.of(userId),null,null,"evt-1"));
  verify(repo,never()).save(any(Notification.class));
 }
}
