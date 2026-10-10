package odk.malicompetences.backend.competence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "secteur_activite",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_secteur_activite_nom",
                        columnNames = "nom"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class SecteurActivite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;
}