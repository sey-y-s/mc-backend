package odk.malicompetences.backend.preuve;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.validation.Validation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "preuve")


@Getter
@Setter
@NoArgsConstructor
public class Preuve {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_competence_id", nullable = false)
    private CitoyenCompetence citoyenCompetence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TypePreuve type;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "fichier_url", length = 500)
    private String fichierUrl;

    @Column(name = "date_depot", nullable = false)
    private LocalDateTime dateDepot;

    @OneToMany(mappedBy = "preuve")
    private List<Validation> validations = new ArrayList<>();
}