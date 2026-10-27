package odk.malicompetences.backend.centre;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.competence.Competence;

import java.time.LocalDate;
import java.util.UUID;

/** Agrément d'un centre pour évaluer une compétence, sur une période facultative. */
@Entity
@Table(name = "centre_competence",
        uniqueConstraints = @UniqueConstraint(name = "uk_centre_competence", columnNames = {"centre_id", "competence_id"}))
@Getter
@Setter
@NoArgsConstructor
public class CentreCompetence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "centre_id", nullable = false)
    private Centre centre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competence_id", nullable = false)
    private Competence competence;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    public boolean valableLe(LocalDate jour) {
        return actif && (dateDebut == null || !jour.isBefore(dateDebut)) && (dateFin == null || !jour.isAfter(dateFin));
    }
}
