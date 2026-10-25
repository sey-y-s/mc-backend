package odk.malicompetences.backend.notification;
import org.springframework.stereotype.Component;
@Component
public class NotificationMapper {
 public NotificationResponse toResponse(Notification n) { return new NotificationResponse(n.getId(), n.getTitre(), n.getContenu(), n.getTypeEvenement(), n.getTypeRessource(), n.getRessourceId(), n.getCreeLe(), n.getLueLe()); }
}
