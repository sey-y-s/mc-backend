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
                @UniqueConstraint(
                        name = "uk_region_nom",
                        columnNames = "nom"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nom;
}