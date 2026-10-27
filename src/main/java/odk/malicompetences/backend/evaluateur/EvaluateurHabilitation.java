package odk.malicompetences.backend.evaluateur;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.competence.Competence;

import java.util.UUID;

/**
 * Compétence que l'évaluateur est habilité à évaluer dans son centre.
 * Sans aucune ligne, l'habilitation couvre toutes les compétences pour lesquelles le centre est agréé.
 */
@Entity
@Table(name = "evaluateur_habilitation",
        uniqueConstraints = @UniqueConstraint(name = "uk_evaluateur_habilitation", columnNames = {"evaluateur_centre_id", "competence_id"}))
@Getter
@Setter
@NoArgsConstructor
public class EvaluateurHabilitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluateur_centre_id", nullable = false)
    private EvaluateurCentre evaluateurCentre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competence_id", nullable = false)
    private Competence competence;
}
