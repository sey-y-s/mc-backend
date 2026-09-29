package odk.malicompetences.backend.miseenrelation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.talent.SuiviTalent;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "demande_mise_en_relation")
@Getter
@Setter
@NoArgsConstructor
public class DemandeMiseEnRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "demandeur_id", nullable = false)
    private Utilisateur demandeur; // <--- C'est ce champ qui génère setDemandeur() et getDemandeur()

    @ManyToOne
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Citoyen destinataire;

    @ManyToOne
    @JoinColumn(name = "suivi_talent_id")
    private SuiviTalent suiviTalent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatutContact statut;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;
}