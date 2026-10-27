package odk.malicompetences.backend.besoin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.organisation.Organisation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "besoin")
@Getter
@Setter
@NoArgsConstructor
public class Besoin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private StatutBesoin statut;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private LocalDateTime dateCreation;

    /** Lieu de la demande (facultatif ; à défaut, position de l'organisation), en degrés décimaux WGS84. */
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    /**
     * Zone géographique du besoin : une commune, ou un territoire (région, district ou cercle).
     * Sans zone, le besoin est national. Pour les statistiques, un besoin est compté une seule fois,
     * dans sa zone la plus précise.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private odk.malicompetences.backend.territoire.Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private odk.malicompetences.backend.territoire.Commune commune;
}