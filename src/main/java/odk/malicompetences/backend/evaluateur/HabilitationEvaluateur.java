package odk.malicompetences.backend.evaluateur;

import jakarta.persistence.*; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter; import odk.malicompetences.backend.centre.Centre; import odk.malicompetences.backend.competence.Competence; import odk.malicompetences.backend.common.statut.StatutHabilitation; import odk.malicompetences.backend.validation.MethodeValidation; import odk.malicompetences.backend.utilisateur.Utilisateur; import java.time.LocalDate; import java.util.UUID;
@Entity @Table(name="habilitation_evaluateur") @Getter @Setter @NoArgsConstructor
public class HabilitationEvaluateur {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="evaluateur_id",nullable=false) private Evaluateur evaluateur;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="competence_id",nullable=false) private Competence competence;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="methode_id",nullable=false) private MethodeValidation methode;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="centre_id") private Centre centre;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="autorise_par_utilisateur_id") private Utilisateur autorisePar;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private StatutHabilitation statut=StatutHabilitation.EN_ATTENTE;
 @Column(name="date_debut") private LocalDate dateDebut;
 @Column(name="date_fin") private LocalDate dateFin;
}
