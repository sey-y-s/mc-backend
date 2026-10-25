package odk.malicompetences.backend.opportunite;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.territoire.Territoire;
import java.time.LocalDate; import java.time.LocalDateTime; import java.util.UUID;
@Entity @Table(name="opportunite") @Getter @Setter @NoArgsConstructor
public class Opportunite {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="categorie_id",nullable=false) private CategorieOpportunite categorie;
 @Column(nullable=false,length=200) private String titre;
 @Column(nullable=false,columnDefinition="TEXT") private String description;
 @Column(nullable=false,length=180) private String organisme;
 @Column(name="lien_externe",length=500) private String lienExterne;
 @Column(length=190) private String contact;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="territoire_id") private Territoire territoire;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private StatutOpportunite statut=StatutOpportunite.BROUILLON;
 @Column(name="publiee_le") private LocalDateTime publieeLe;
 @Column(name="date_expiration") private LocalDate dateExpiration;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="cree_par_utilisateur_id",nullable=false) private Utilisateur creePar;
 @Column(name="cree_le",nullable=false,updatable=false) private LocalDateTime creeLe;
 @Column(name="modifie_le",nullable=false) private LocalDateTime modifieLe;
 @PrePersist void creation(){if(creeLe==null)creeLe=LocalDateTime.now();if(modifieLe==null)modifieLe=LocalDateTime.now();if(statut==StatutOpportunite.PUBLIEE&&publieeLe==null)publieeLe=LocalDateTime.now();}
 @PreUpdate void modification(){modifieLe=LocalDateTime.now();if(statut==StatutOpportunite.PUBLIEE&&publieeLe==null)publieeLe=LocalDateTime.now();}
}
