package odk.malicompetences.backend.evaluateur;

import jakarta.persistence.*; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter; import odk.malicompetences.backend.citoyen.Citoyen; import odk.malicompetences.backend.common.statut.StatutEvaluateur; import java.util.UUID;
@Entity @Table(name="evaluateur") @Getter @Setter @NoArgsConstructor
public class Evaluateur {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="citoyen_id",nullable=false,unique=true) private Citoyen citoyen;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private StatutEvaluateur statut=StatutEvaluateur.EN_ATTENTE;
}
