package odk.malicompetences.backend.competence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "metier")
@Getter
@Setter
@NoArgsConstructor
public class Metier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "secteur_id", nullable = false)
    private SecteurActivite secteurActivite;

    @Column(name = "source_referentiel_id")
    private UUID sourceReferentielId;

    @Column(unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 180)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean actif = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "metier_competence",
            joinColumns = @JoinColumn(name = "metier_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id")
    )
    private Set<Competence> competences = new LinkedHashSet<>();
}
