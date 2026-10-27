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

    /**
     * Secteurs par lesquels cette compétence peut s'acquérir, séparés par une virgule : « FORMEL,INFORMEL » (défaut)
     * ou « FORMEL » seul (ex. science des données, qui suppose une formation).
     */
    @Column(name = "parcours_possibles", nullable = false, length = 30)
    private String parcoursPossibles = "FORMEL,INFORMEL";

    /** Secteurs possibles sous forme de liste (jamais vide). */
    public java.util.List<ParcoursCompetence> getParcoursAutorises() {
        if (parcoursPossibles == null || parcoursPossibles.isBlank()) {
            return java.util.List.of(ParcoursCompetence.FORMEL, ParcoursCompetence.INFORMEL);
        }
        java.util.List<ParcoursCompetence> liste = java.util.Arrays.stream(parcoursPossibles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(ParcoursCompetence::valueOf)
                .distinct()
                .sorted()
                .toList();
        return liste.isEmpty() ? java.util.List.of(ParcoursCompetence.FORMEL, ParcoursCompetence.INFORMEL) : liste;
    }

    public void setParcoursAutorises(java.util.Collection<ParcoursCompetence> parcours) {
        this.parcoursPossibles = parcours == null || parcours.isEmpty()
                ? "FORMEL,INFORMEL"
                : parcours.stream().distinct().sorted().map(Enum::name).collect(java.util.stream.Collectors.joining(","));
    }
}