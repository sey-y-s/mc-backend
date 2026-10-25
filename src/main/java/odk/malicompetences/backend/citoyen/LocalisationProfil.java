package odk.malicompetences.backend.citoyen;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.common.statut.SourceLocalisation;
import odk.malicompetences.backend.territoire.Commune;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "localisation_profil")
@Getter @Setter @NoArgsConstructor
public class LocalisationProfil {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_id", nullable = false, unique = true)
    private Citoyen citoyen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commune_id", nullable = false)
    private Commune commune;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SourceLocalisation source = SourceLocalisation.DECLARATION;

    @Column(name = "confirmee_le", nullable = false)
    private LocalDateTime confirmeeLe = LocalDateTime.now();

    @PrePersist protected void initialiser() {
        if (confirmeeLe == null) confirmeeLe = LocalDateTime.now();
    }
}
