package odk.malicompetences.backend.miseenrelation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.organisation.Organisation;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "consultation_profil") @Getter @Setter @NoArgsConstructor
public class ConsultationProfil {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private Organisation organisation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "citoyen_id", nullable = false) private Citoyen citoyen;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "besoin_id") private Besoin besoin;
    @Column(name = "consultee_le", nullable = false, insertable = false, updatable = false) private LocalDateTime dateConsultation;
}
