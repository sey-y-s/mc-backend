package odk.malicompetences.backend.validation;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.preuve.Preuve;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "validation")
@NoArgsConstructor
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_competence_id", nullable = false)
    private CitoyenCompetence citoyenCompetence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preuve_id")
    private Preuve preuve;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "validee_par_id", nullable = false)
    private Utilisateur valideePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centre_id")
    private Centre centre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TypeValidation type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutValidation statut;

    @Column(name = "date_demande", nullable = false, updatable = false)
    private LocalDateTime dateDemande;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @PrePersist
    protected void onCreate() {
        if (dateDemande == null) {
            dateDemande = LocalDateTime.now();
        }
    }
}