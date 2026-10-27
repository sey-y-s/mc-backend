package odk.malicompetences.backend.territoire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "commune",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_commune_region_nom",
                        columnNames = {"region_id", "nom"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Commune {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false, length = 100)
    private String nom;

    /**
     * Cercle de rattachement (territoire de niveau CERCLE dont la région parente est {@link #region}).
     * Nul pour les communes du district de Bamako.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cercle_id")
    private Region cercle;

    /** Position de référence de la commune (référentiel, jamais une position de personne). */
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
}