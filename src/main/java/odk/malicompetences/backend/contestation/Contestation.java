package odk.malicompetences.backend.contestation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.validation.Validation;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contestation d'une décision ou demande de correction d'une donnée du profil.
 * Une contestation acceptée sur une décision produit une NOUVELLE décision (nouvelleDecision),
 * l'ancienne n'est jamais modifiée.
 */
@Entity
@Table(name = "contestation")
@Getter
@Setter
@NoArgsConstructor
public class Contestation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citoyen citoyen;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_objet", nullable = false, length = 30)
    private TypeObjetContestation typeObjet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_id")
    private Validation validation;

    @Column(name = "champ_concerne", length = 60)
    private String champConcerne;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutContestation statut = StatutContestation.OUVERTE;

    @Column(columnDefinition = "TEXT")
    private String reponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traitee_par_id")
    private Utilisateur traiteePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nouvelle_decision_id")
    private Validation nouvelleDecision;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    @PrePersist
    void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
    }
}
