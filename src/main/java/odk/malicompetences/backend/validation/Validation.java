package odk.malicompetences.backend.validation;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.preuve.Preuve;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
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

    /** Niveau reconnu par la décision (une décision favorable fixe le niveau validé de la compétence). */
    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_valide", length = 30)
    private odk.malicompetences.backend.competence.NiveauCompetence niveauValide;

    /**
     * Décision corrigée par celle-ci (contestation acceptée). Les décisions ne sont jamais modifiées :
     * toute correction est une nouvelle décision qui référence l'ancienne.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_corrigee_id")
    private Validation decisionCorrigee;

    @PrePersist
    protected void onCreate() {
        if (dateDemande == null) {
            dateDemande = LocalDateTime.now();
        }
    }
}