package odk.malicompetences.backend.testnumerique;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.competence.Competence;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name="test_numerique", uniqueConstraints=@UniqueConstraint(name="uq_test_competence_version", columnNames={"competence_id", "version"}))
@Getter @Setter @NoArgsConstructor
public class TestNumerique {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="competence_id", nullable=false) private Competence competence;
    @Column(nullable=false, length=180) private String titre;
    @Column(columnDefinition="TEXT") private String description;
    @Column(nullable=false, length=40) private String version;
    @Column(name="seuil_reussite", nullable=false, precision=5, scale=2) private BigDecimal seuilReussite = BigDecimal.valueOf(60);
    @Column(nullable=false) private boolean actif = true;
    @Column(name="cree_le", nullable=false, updatable=false) private LocalDateTime creeLe;
    @PrePersist void prePersist() { if (creeLe == null) creeLe = LocalDateTime.now(); }
}
