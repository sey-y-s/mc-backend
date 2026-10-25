package odk.malicompetences.backend.besoin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.Territoire;

import java.util.UUID;

/** Zone couverte par un besoin : une commune ou un territoire, jamais les deux. */
@Entity
@Table(name = "besoin_zone")
@Getter
@Setter
@NoArgsConstructor
public class BesoinZone {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "besoin_id", nullable = false)
    private Besoin besoin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private Commune commune;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "territoire_id")
    private Territoire territoire;
}
