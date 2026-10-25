package odk.malicompetences.backend.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification", uniqueConstraints = @UniqueConstraint(name = "uq_notification_dedup", columnNames = {"utilisateur_id", "cle_deduplication"}), indexes = @Index(name = "idx_notification_inbox", columnList = "utilisateur_id,lue_le,cree_le"))
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "utilisateur_id", nullable = false) private Utilisateur utilisateur;
    @Column(name = "type_evenement", nullable = false, length = 60) private String typeEvenement;
    @Column(nullable = false, length = 180) private String titre;
    @Column(nullable = false, length = 1000) private String contenu;
    @Column(name = "type_ressource", length = 80) private String typeRessource;
    @Column(name = "ressource_id") private UUID ressourceId;
    @Column(name = "cree_le", nullable = false, updatable = false) private LocalDateTime creeLe;
    @Column(name = "lue_le") private LocalDateTime lueLe;
    @Column(name = "cle_deduplication", length = 190) private String cleDeduplication;
    @PrePersist void avantCreation() { if (creeLe == null) creeLe = LocalDateTime.now(); }
}
