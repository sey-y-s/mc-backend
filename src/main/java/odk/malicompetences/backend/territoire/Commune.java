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
}
