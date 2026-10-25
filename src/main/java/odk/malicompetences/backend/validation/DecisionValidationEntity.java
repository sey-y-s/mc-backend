package odk.malicompetences.backend.validation;

import jakarta.persistence.*; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import odk.malicompetences.backend.common.statut.DecisionValidation; import odk.malicompetences.backend.competence.NiveauCompetence; import odk.malicompetences.backend.utilisateur.Utilisateur; import java.time.LocalDateTime; import java.util.UUID;
@Entity @Table(name="decision_validation") @Getter @Setter @NoArgsConstructor
public class DecisionValidationEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="demande_validation_id",nullable=false) private DemandeValidation demande;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="decideur_utilisateur_id") private Utilisateur decideur;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=25) private DecisionValidation decision;
 @Enumerated(EnumType.STRING) @Column(name="niveau_valide",length=20) private NiveauCompetence niveauValide;
 @Column(columnDefinition="TEXT") private String motif;
 @Column(name="decidee_le",nullable=false,updatable=false) private LocalDateTime decideeLe;
 @PrePersist void prePersist(){if(decideeLe==null)decideeLe=LocalDateTime.now();}
}
