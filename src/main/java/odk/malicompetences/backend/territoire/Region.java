package odk.malicompetences.backend.territoire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "region",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_region_nom", columnNames = "nom"),
                @UniqueConstraint(name = "uk_region_code_stable", columnNames = "code_stable")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code_stable", length = 32)
    private String codeStable;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau", nullable = false, length = 16)
    private NiveauTerritoire niveau = NiveauTerritoire.REGION;

    @Column(nullable = false, length = 100)
    private String nom;

    /** Région parente : renseignée uniquement pour un CERCLE (une région ou le district n'ont pas de parent). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Region parent;

    /** Chef-lieu de la région, en degrés décimaux WGS84. */
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
}