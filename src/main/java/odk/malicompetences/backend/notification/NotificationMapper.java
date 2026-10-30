package odk.malicompetences.backend.notification;

import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationRequest request) {
        if (request == null) {
            return null;
        }
        Notification notification = new Notification();
        notification.setTitre(request.titre());
        notification.setMessage(request.message());
        notification.setType(request.type());
                notification.setDateCreation(LocalDateTime.now());
        notification.setReferenceId(request.referenceId());
        return notification;
    }

    public NotificationDestinataire toDestinataireEntity(Notification notification, Utilisateur utilisateur) {
        if (notification == null || utilisateur == null) {
            return null;
        }
        NotificationDestinataire nd = new NotificationDestinataire();
        nd.setNotification(notification);
        nd.setUtilisateur(utilisateur);
        nd.setLu(false);
        nd.setDateReception(LocalDateTime.now());
        return nd;
    }

    public NotificationResponse toResponse(NotificationDestinataire nd) {
        if (nd == null || nd.getNotification() == null) {
            return null;
        }
        Notification n = nd.getNotification();
        return new NotificationResponse(
                n.getId(),
                nd.getId(),
                n.getTitre(),
                n.getMessage(),
                n.getType(),
                nd.getLu(),
                nd.getDateReception(),
                nd.getDateLecture(),
                                n.getDateCreation(),
                n.getReferenceId()
        );
    }
}