package odk.malicompetences.backend.testnumerique;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "question")
@Getter @Setter @NoArgsConstructor
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private TestNumerique test;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texte;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeQuestion type;
    @Column(nullable = false)
    private Integer ordre;
}
