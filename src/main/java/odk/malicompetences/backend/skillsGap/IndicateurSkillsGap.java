package odk.malicompetences.backend.skillsGap;

import jakarta.persistence.*;

import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.territoire.Region;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "indicateur_skills_gap")
public class IndicateurSkillsGap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competence_id", nullable = false)
    private Competence competence;

    @Column(name = "periode_debut", nullable = false)
    private LocalDate periodeDebut;

    @Column(name = "periode_fin", nullable = false)
    private LocalDate periodeFin;

    @Column(name = "date_calcul", nullable = false)
    private LocalDateTime dateCalcul;

    @Column(name = "demande_estimee", nullable = false)
    private Integer demandeEstimee;

    @Column(name = "offre_disponible", nullable = false)
    private Integer offreDisponible;

    @Column(nullable = false)
    private Integer ecart;
}