package odk.malicompetences.backend.validation;

import jakarta.persistence.*; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import odk.malicompetences.backend.centre.Centre; import odk.malicompetences.backend.competence.CitoyenCompetence; import odk.malicompetences.backend.common.statut.StatutDemandeValidation; import java.time.LocalDateTime; import java.util.UUID;
@Entity @Table(name="demande_validation") @Getter @Setter @NoArgsConstructor
public class DemandeValidation {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="citoyen_competence_id",nullable=false) private CitoyenCompetence citoyenCompetence;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="methode_id",nullable=false) private MethodeValidation methode;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="centre_id") private Centre centre;
 @Column(name="evaluateur_assigne_id") private UUID evaluateurAssigneId;
 @Column(name="message_citoyen",columnDefinition="TEXT") private String messageCitoyen;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=25) private StatutDemandeValidation statut=StatutDemandeValidation.EN_ATTENTE;
 @Column(name="creee_le",nullable=false,updatable=false) private LocalDateTime creeeLe;
 @Column(name="cloturee_le") private LocalDateTime clotureeLe;
 @PrePersist void prePersist(){if(creeeLe==null)creeeLe=LocalDateTime.now();}
}
