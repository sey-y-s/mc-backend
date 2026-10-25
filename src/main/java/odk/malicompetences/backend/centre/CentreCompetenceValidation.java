package odk.malicompetences.backend.centre;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.competence.Competence;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "centre_competence_validation")
@Getter @Setter @NoArgsConstructor
public class CentreCompetenceValidation {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "centre_id", nullable = false) private Centre centre;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "competence_id", nullable = false) private Competence competence;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "valide_du") private LocalDate valideDu;
    @Column(name = "valide_au") private LocalDate valideAu;
    public boolean couvre(LocalDate date) { return active && (valideDu == null || !date.isBefore(valideDu)) && (valideAu == null || !date.isAfter(valideAu)); }
}
