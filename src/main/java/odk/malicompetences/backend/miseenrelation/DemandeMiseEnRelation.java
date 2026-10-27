package odk.malicompetences.backend.miseenrelation;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.talent.SuiviBesoinTalent;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "demande_mise_en_relation")
public class DemandeMiseEnRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demandeur_id", nullable = false)
    private Utilisateur demandeur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Citoyen destinataire;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suivi_besoin_talent_id")
    private SuiviBesoinTalent suiviBesoinTalent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutContact statut;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;
}
