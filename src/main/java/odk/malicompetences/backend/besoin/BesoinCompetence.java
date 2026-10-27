package odk.malicompetences.backend.besoin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.NiveauCompetence;

import java.util.UUID;

@Entity
@Table(
        name = "besoin_competence",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_besoin_competence",
                        columnNames = {"besoin_id", "competence_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BesoinCompetence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "besoin_id", nullable = false)
    private Besoin besoin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competence_id", nullable = false)
    private Competence competence;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_minimum", nullable = false, length = 30)
    private NiveauCompetence niveauMinimum;

    @Column(nullable = false)
    private Integer quantite;
}