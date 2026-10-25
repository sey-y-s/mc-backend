package odk.malicompetences.backend.organisation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Justificatif déposé par une organisation ; le dépôt ne vaut pas vérification. */
@Entity
@Table(name = "justificatif_organisation")
@Getter
@Setter
@NoArgsConstructor
public class JustificatifOrganisation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "type_justificatif", nullable = false, length = 40)
    private String typeJustificatif;

    @Column(name = "reference_document", length = 180)
    private String referenceDocument;

    @Column(name = "cle_stockage_prive", length = 500)
    private String cleStockagePrive;

    @Column(name = "date_emission")
    private LocalDate dateEmission;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutJustificatifOrganisation statut = StatutJustificatifOrganisation.RENSEIGNE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verifie_par_utilisateur_id")
    private Utilisateur verifiePar;

    @Column(name = "verifie_le")
    private LocalDateTime verifieLe;

    @Column(name = "motif_rejet", columnDefinition = "TEXT")
    private String motifRejet;
}
