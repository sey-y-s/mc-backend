package odk.malicompetences.backend.opportunite;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "categorie_opportunite")
@Getter
@Setter
@NoArgsConstructor
public class CategorieOpportunite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nom; // <--- C'est ce champ qui génère setNom() et getNom()

    @Column(columnDefinition = "TEXT")
    private String description;
}