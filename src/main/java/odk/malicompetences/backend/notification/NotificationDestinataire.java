package odk.malicompetences.backend.notification;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "notification_destinataire",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_destinataire",
                        columnNames = {"notification_id", "utilisateur_id"}
                )
        }
)
public class NotificationDestinataire {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private Boolean lu = false;

    @Column(name = "date_reception", nullable = false)
    private LocalDateTime dateReception;

    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;
}