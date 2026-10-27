package odk.malicompetences.backend.opportunite;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.citoyen.Citoyen;

import java.time.LocalDateTime;
import java.util.UUID;

/** Candidature d'un citoyen à une opportunité (une seule par citoyen et par opportunité). */
@Entity
@Table(name = "candidature_opportunite",
        uniqueConstraints = @UniqueConstraint(name = "uk_candidature_opportunite_citoyen", columnNames = {"opportunite_id", "citoyen_id"}))
@Getter
@Setter
@NoArgsConstructor
public class CandidatureOpportunite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunite_id", nullable = false)
    private Opportunite opportunite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citoyen citoyen;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCandidature statut = StatutCandidature.ENVOYEE;

    /** Le citoyen habite la localité de l'opportunité (commune ou région) au moment de sa candidature. */
    @Column(nullable = false)
    private boolean locale;

    @Column(name = "date_candidature", nullable = false)
    private LocalDateTime dateCandidature;
}
