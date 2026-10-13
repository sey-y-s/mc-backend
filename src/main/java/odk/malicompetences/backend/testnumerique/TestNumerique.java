package odk.malicompetences.backend.testnumerique;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.competence.Competence;

import java.util.UUID;

@Entity
@Table(name = "test_numerique")
@Getter @Setter @NoArgsConstructor
public class TestNumerique {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competence_id", nullable = false)
    private Competence competence;
    @Column(nullable = false, length = 200)
    private String titre;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private boolean actif = true;
}
