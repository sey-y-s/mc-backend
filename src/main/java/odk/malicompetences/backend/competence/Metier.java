package odk.malicompetences.backend.competence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "metier",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_metier_secteur_nom",
                        columnNames = {"secteur_activite_id", "nom"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Metier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "secteur_activite_id", nullable = false)
    private SecteurActivite secteurActivite;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;
}