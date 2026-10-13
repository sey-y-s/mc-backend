package odk.malicompetences.backend.competence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "competence",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_competence_metier_nom",
                        columnNames = {"metier_id", "nom"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Competence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metier_id", nullable = false)
    private Metier metier;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;
}