package odk.malicompetences.backend.opportunite;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
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

    /** Localité concernée : région (et commune facultative). Vide = tout le Mali. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private odk.malicompetences.backend.territoire.Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private odk.malicompetences.backend.territoire.Commune commune;

    /** Date d'envoi de l'alerte aux citoyens de la localité (une seule fois). */
    @Column(name = "date_diffusion")
    private java.time.LocalDateTime dateDiffusion;
}