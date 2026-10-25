package odk.malicompetences.backend.notification;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service @Transactional
public class NotificationService {
 private final NotificationRepository repository;
 private final UtilisateurRepository utilisateurs;
 private final NotificationMapper mapper;
 public NotificationService(NotificationRepository repository, UtilisateurRepository utilisateurs, NotificationMapper mapper) { this.repository=repository; this.utilisateurs=utilisateurs; this.mapper=mapper; }
 public void creerEtEnvoyerNotification(NotificationRequest request) {
   for (UUID id : request.destinatairesIds().stream().distinct().toList()) {
     Utilisateur utilisateur=utilisateurs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : "+id));
     if (request.cleDeduplication()!=null && repository.existsByUtilisateur_IdAndCleDeduplication(id, request.cleDeduplication())) continue;
     Notification n=new Notification(); n.setUtilisateur(utilisateur); n.setTitre(request.titre()); n.setContenu(request.contenu()); n.setTypeEvenement(request.type().name()); n.setTypeRessource(request.typeRessource()); n.setRessourceId(request.ressourceId()); n.setCleDeduplication(request.cleDeduplication());
     try { repository.save(n); } catch (DataIntegrityViolationException e) { if (request.cleDeduplication()==null) throw e; }
   }
 }
 @Transactional(readOnly=true) public List<NotificationResponse> recupererNotificationsParUtilisateur(UUID id) { return repository.findByUtilisateur_IdOrderByCreeLeDesc(id).stream().map(mapper::toResponse).toList(); }
 @Transactional(readOnly=true) public List<NotificationResponse> recupererNotificationsNonLues(UUID id) { return repository.findByUtilisateur_IdAndLueLeIsNullOrderByCreeLeDesc(id).stream().map(mapper::toResponse).toList(); }
 public NotificationResponse marquerCommeLue(UUID notificationId, UUID utilisateurId, boolean administrateur) {
   Notification n=(administrateur ? repository.findById(notificationId) : repository.findByIdAndUtilisateur_Id(notificationId, utilisateurId)).orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
   if (!administrateur && !n.getUtilisateur().getId().equals(utilisateurId)) throw new org.springframework.security.access.AccessDeniedException("Accès refusé");
   if(n.getLueLe()==null) n.setLueLe(LocalDateTime.now()); return mapper.toResponse(repository.save(n));
 }
}
