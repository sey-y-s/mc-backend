package odk.malicompetences.backend.talent;


import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.organisation.Organisation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
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
    @JoinColumn(name = "besoin_id", nullable = false)
    private Besoin besoin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citoyen citoyen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutSuiviBesoinTalent statut;

    @Transient private BigDecimal scoreCorrespondance;

    @Transient private String detailsCorrespondance;

    @Column(name = "note_interne", columnDefinition = "TEXT")
    private String noteInterne;

    @Column(name = "cree_le", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "modifie_le", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dateMiseAJour;

    @PrePersist
    void initialiserHorodatages() {
        LocalDateTime maintenant = LocalDateTime.now();
        if (dateCreation == null) dateCreation = maintenant;
        if (dateMiseAJour == null) dateMiseAJour = maintenant;
    }

    @PreUpdate
    void actualiserHorodatageModification() {
        dateMiseAJour = LocalDateTime.now();
    }
}
