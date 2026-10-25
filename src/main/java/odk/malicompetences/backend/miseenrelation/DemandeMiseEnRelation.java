package odk.malicompetences.backend.miseenrelation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.talent.SuiviBesoinTalent;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "demande_mise_en_relation")
public class DemandeMiseEnRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demandeur_utilisateur_id", nullable = false)
    private Utilisateur demandeur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinataire_citoyen_id", nullable = false)
    private Citoyen destinataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suivi_besoin_talent_id")
    private SuiviBesoinTalent suiviBesoinTalent;

    /** Clé nullable alimentée par le service uniquement tant que la demande est en attente. */
    @Column(name = "en_attente_cle", length = 36)
    private UUID enAttenteCle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutContact statut;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "creee_le", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dateDemande;

    @Column(name = "repondue_le")
    private LocalDateTime dateReponse;
}
