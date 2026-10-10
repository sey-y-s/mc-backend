package odk.malicompetences.backend.opportunite;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "opportunite")
public class Opportunite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private CategorieOpportunite categorie;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TypeOpportunite type;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private StatutOpportunite statut;

    @Column(name = "date_publication")
    private LocalDateTime datePublication;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;
}