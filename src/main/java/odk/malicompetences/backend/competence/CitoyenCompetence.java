package odk.malicompetences.backend.competence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.experience.Experience;

import java.util.HashSet;
import java.util.Set;

import java.time.LocalDateTime;
import java.util.UUID;

import odk.malicompetences.backend.preuve.Preuve;
import odk.malicompetences.backend.validation.Validation;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "citoyen_competence",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_citoyen_competence",
                        columnNames = {"citoyen_id", "competence_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CitoyenCompetence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citoyen citoyen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competence_id", nullable = false)
    private Competence competence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NiveauCompetence niveau;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EtatCompetence etat;

    @Column(name = "date_declaration", nullable = false)
    private LocalDateTime dateDeclaration;

    @ManyToMany(mappedBy = "competences")
    private Set<Experience> experiences = new HashSet<>();

    @OneToMany(
            mappedBy = "citoyenCompetence",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Preuve> preuves = new ArrayList<>();

    @OneToMany(
            mappedBy = "citoyenCompetence",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Validation> validations = new ArrayList<>();
}