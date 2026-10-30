package odk.malicompetences.backend.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titre; // <--- C'est ce champ qui génère setTitre() et getTitre()

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private TypeNotification type; // Adaptez selon votre type d'enum

        @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    /** Objet concerné (demande de mise en relation, validation...) : l'application l'ouvre directement. */
    @Column(name = "reference_id")
    private UUID referenceId;
}