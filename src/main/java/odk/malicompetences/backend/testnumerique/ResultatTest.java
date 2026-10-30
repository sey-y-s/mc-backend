package odk.malicompetences.backend.testnumerique;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.preuve.Preuve;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resultat_test")
@Getter @Setter @NoArgsConstructor
public class ResultatTest {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private TestNumerique test;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citoyen citoyen;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;
    @Column(nullable = false)
    private Boolean reussi;
    @Column(name = "date_passage", nullable = false)
    private LocalDateTime datePassage;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preuve_id", unique = true)
    private Preuve preuve;

    /** Non enregistré : vrai quand ce test réussi a validé la compétence (parcours « test numérique »). */
    @Transient
    private boolean competenceValidee;
}
