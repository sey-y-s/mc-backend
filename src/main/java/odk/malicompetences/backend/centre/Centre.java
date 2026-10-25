package odk.malicompetences.backend.centre;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.common.statut.StatutCentre;
import odk.malicompetences.backend.territoire.Commune;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "centre")
@Getter
@Setter
@NoArgsConstructor
public class Centre {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 180)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String adresse;

    @Column(length = 30)
    private String telephone;

    @Column(length = 190)
    private String email;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCentre statut = StatutCentre.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commune_id", nullable = false)
    private Commune commune;

    @PrePersist
    void initialiserDateCreation() {
        if (creeLe == null) creeLe = LocalDateTime.now();
    }
}
