package odk.malicompetences.backend.notification;
import jakarta.persistence.*; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter; import odk.malicompetences.backend.utilisateur.Utilisateur; import java.time.LocalDateTime; import java.util.UUID;
@Entity @Table(name="appareil_notification") @Getter @Setter @NoArgsConstructor
public class AppareilNotification {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="utilisateur_id",nullable=false) private Utilisateur utilisateur;
 @Column(name="jeton_hash",nullable=false,unique=true,length=255) private String jetonHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private PlateformeAppareil plateforme;
 @Column(nullable=false) private boolean actif=true;
 @Column(name="derniere_activite") private LocalDateTime derniereActivite;
 @Column(name="cree_le",nullable=false,updatable=false) private LocalDateTime creeLe;
 @PrePersist void creation(){if(creeLe==null)creeLe=LocalDateTime.now();}
}
