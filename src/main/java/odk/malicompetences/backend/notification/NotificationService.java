package odk.malicompetences.backend.notification;

import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDestinataireRepository notificationDestinataireRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationDestinataireRepository notificationDestinataireRepository,
            UtilisateurRepository utilisateurRepository,
            NotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationDestinataireRepository = notificationDestinataireRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationMapper = notificationMapper;
    }

    // Créer et diffuser une notification à un ou plusieurs utilisateurs
    public void creerEtEnvoyerNotification(NotificationRequest request) {
        Notification notification = notificationMapper.toEntity(request);
        Notification savedNotification = notificationRepository.save(notification);

        for (UUID utilisateurId : request.destinatairesIds()) {
            Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + utilisateurId));

            NotificationDestinataire destinataire = notificationMapper.toDestinataireEntity(savedNotification, utilisateur);
            notificationDestinataireRepository.save(destinataire);
        }
    }

    // Récupérer toutes les notifications d'un utilisateur
    @Transactional(readOnly = true)
    public List<NotificationResponse> recupererNotificationsParUtilisateur(UUID utilisateurId) {
        return notificationDestinataireRepository.findAll().stream()
                .filter(nd -> nd.getUtilisateur().getId().equals(utilisateurId))
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Récupérer uniquement les notifications non lues d'un utilisateur
    @Transactional(readOnly = true)
    public List<NotificationResponse> recupererNotificationsNonLues(UUID utilisateurId) {
        return notificationDestinataireRepository.findAll().stream()
                .filter(nd -> nd.getUtilisateur().getId().equals(utilisateurId) && Boolean.FALSE.equals(nd.getLu()))
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Marquer une notification comme lue
    public NotificationResponse marquerCommeLue(
            UUID notificationDestinataireId,
            UUID authenticatedUserId,
            boolean administrateur
    ) {
        NotificationDestinataire nd = (administrateur
                ? notificationDestinataireRepository.findById(notificationDestinataireId)
                : notificationDestinataireRepository.findByIdAndUtilisateurId(
                        notificationDestinataireId, authenticatedUserId
                )).orElseThrow(() ->
                new ResourceNotFoundException("Notification destinataire introuvable"));

        if (Boolean.FALSE.equals(nd.getLu())) {
            nd.setLu(true);
            nd.setDateLecture(LocalDateTime.now());
            nd = notificationDestinataireRepository.save(nd);
        }

        return notificationMapper.toResponse(nd);
    }
}