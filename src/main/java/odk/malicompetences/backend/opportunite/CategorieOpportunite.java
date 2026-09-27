package odk.malicompetences.backend.opportunite;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "categorie_opportunite")
public class CategorieOpportunite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150, unique = true)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;
}