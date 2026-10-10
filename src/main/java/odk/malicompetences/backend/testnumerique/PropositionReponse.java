package odk.malicompetences.backend.testnumerique;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "proposition_reponse")
@Getter @Setter @NoArgsConstructor
public class PropositionReponse {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texte;
    @Column(nullable = false)
    private boolean correcte;
}
