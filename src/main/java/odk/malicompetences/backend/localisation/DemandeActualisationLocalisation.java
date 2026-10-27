package odk.malicompetences.backend.localisation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.territoire.Commune;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Proposition de changement de commune issue d'une détection GPS ponctuelle.
 * Ne contient AUCUNE coordonnée : seulement la commune suggérée. Jamais appliquée sans confirmation.
 * La colonne générée en_attente_cle (non mappée) garantit en base une seule demande EN_ATTENTE par citoyen.
 */
@Entity
@Table(name = "demande_actualisation_localisation")
@Getter
@Setter
@NoArgsConstructor
public class DemandeActualisationLocalisation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citoyen citoyen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commune_suggeree_id", nullable = false)
    private Commune communeSuggeree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutActualisation statut = StatutActualisation.EN_ATTENTE;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;

    @PrePersist
    void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
    }
}
