package odk.malicompetences.backend.opportunite;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "categorie_opportunite", uniqueConstraints = @UniqueConstraint(name = "uk_categorie_opportunite_nom", columnNames = "nom"))
@Getter
@Setter
@NoArgsConstructor
public class CategorieOpportunite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;
}