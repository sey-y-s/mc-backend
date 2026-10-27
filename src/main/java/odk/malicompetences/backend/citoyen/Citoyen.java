package odk.malicompetences.backend.citoyen;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import odk.malicompetences.backend.portfolio.PortfolioRealisation;
import odk.malicompetences.backend.territoire.Commune;

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

@Entity
@Table(name = "citoyen")

@Getter
@Setter
@NoArgsConstructor
public class Citoyen {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId                                                      // indique que le Citoyen utilise l'id de son Utilisateur.
    @JoinColumn(name = "id")
    private Utilisateur utilisateur;

    @Column(name = "code_passeport",nullable = false, unique = true, length = 14)
    private String codePasseport;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexe sexe;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private Commune commune;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Disponibilite disponibilite = Disponibilite.DISPONIBLE;

    /**
     * Consentement à la détection ponctuelle de la commune par GPS (consignes V1.2) :
     * aucune coordonnée n'est conservée, seule la commune de référence l'est.
     */
    @Column(name = "gps_consenti", nullable = false)
    private boolean gpsConsenti = false;

    @Column(name = "gps_consenti_le")
    private java.time.LocalDateTime gpsConsentiLe;

    /** Origine de la commune de référence : DECLARATION, GPS_CONFIRMEE ou ADMINISTRATION. */
    @Enumerated(EnumType.STRING)
    @Column(name = "localisation_source", length = 20)
    private odk.malicompetences.backend.localisation.SourceLocalisation localisationSource;

    @Column(name = "localisation_confirmee_le")
    private java.time.LocalDateTime localisationConfirmeeLe;

    @OneToMany(
            mappedBy = "citoyen",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PortfolioRealisation> portfolioRealisations = new ArrayList<>();

    /**
     * Change la commune de référence en traçant son origine. La date de confirmation n'est
     * mise à jour que si la commune change réellement.
     */
    public void definirCommuneReference(Commune nouvelle, odk.malicompetences.backend.localisation.SourceLocalisation source) {
        java.util.UUID avant = commune == null ? null : commune.getId();
        java.util.UUID apres = nouvelle == null ? null : nouvelle.getId();
        this.commune = nouvelle;
        if (nouvelle == null) {
            this.localisationSource = null;
            this.localisationConfirmeeLe = null;
        } else if (!java.util.Objects.equals(avant, apres) || localisationSource == null) {
            this.localisationSource = source;
            this.localisationConfirmeeLe = java.time.LocalDateTime.now();
        }
    }
}
