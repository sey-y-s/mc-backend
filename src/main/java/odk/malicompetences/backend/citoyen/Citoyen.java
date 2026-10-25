package odk.malicompetences.backend.citoyen;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.common.statut.StatutActivite;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.experience.Experience;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.territoire.Commune;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identifiant_public", nullable = false, unique = true, updatable = false, length = 36)
    private UUID identifiantPublic;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(nullable = false, length = 120)
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexe sexe;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_activite", nullable = false, length = 30)
    private StatutActivite statutActivite = StatutActivite.NON_RENSEIGNE;

    @Column(name = "profil_public", nullable = false)
    private boolean profilPublic;

    @Column(name = "gps_consenti", nullable = false)
    private boolean gpsConsenti;

    @Column(name = "gps_consenti_le")
    private LocalDateTime gpsConsentiLe;

    @OneToOne(mappedBy = "citoyen", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private LocalisationProfil localisationProfil;

    @OneToMany(mappedBy = "citoyen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CitoyenCompetence> competences = new ArrayList<>();

    @OneToMany(mappedBy = "citoyen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences = new ArrayList<>();

    @PrePersist
    protected void initialiserIdentifiants() {
        if (identifiantPublic == null) identifiantPublic = UUID.randomUUID();
    }

    @Transient public boolean isDisponible() {
        return competences != null && competences.stream().anyMatch(CitoyenCompetence::isDisponibilite);
    }
    @Transient public Commune getCommune() { return localisationProfil == null ? null : localisationProfil.getCommune(); }
    @Transient public void setCommune(Commune commune) {
        if (commune == null) { this.localisationProfil = null; return; }
        if (this.localisationProfil == null) this.localisationProfil = new LocalisationProfil();
        this.localisationProfil.setCitoyen(this);
        this.localisationProfil.setCommune(commune);
    }
}
