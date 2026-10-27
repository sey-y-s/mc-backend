package odk.malicompetences.backend.talent;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.organisation.Organisation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "suivi_besoin_talent",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_suivi_besoin_talent_besoin_citoyen",
                        columnNames = {"besoin_id", "citoyen_id"}
                )
        }
)
public class SuiviBesoinTalent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "besoin_id", nullable = false)
    private Besoin besoin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citoyen citoyen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutSuiviBesoinTalent statut;

    @Column(name = "score_correspondance", precision = 5, scale = 2)
    private BigDecimal scoreCorrespondance;

    @Column(name = "details_correspondance", columnDefinition = "JSON")
    private String detailsCorrespondance;

    @Column(name = "note_interne", columnDefinition = "TEXT")
    private String noteInterne;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_mise_a_jour", nullable = false)
    private LocalDateTime dateMiseAJour;
}
