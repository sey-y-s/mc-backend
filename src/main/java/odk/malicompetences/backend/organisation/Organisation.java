package odk.malicompetences.backend.organisation;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import java.util.UUID;

@Entity
@Table(name = "organisation")
@Getter
@Setter
@NoArgsConstructor
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Column(name = "raison_sociale", nullable = false, length = 200)
    private String nom;

    @Column(name = "nom_commercial", length = 200)
    private String nomCommercial;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "rccm", length = 100)
    private String numeroRccm;

    @Column(name = "nif", length = 100)
    private String nif;

    @Column(name = "telephone", length = 30)
    private String telephone;

    @Column(name = "email", length = 190)
    private String email;

    @Column(name = "adresse", length = 255)
    private String adresse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private odk.malicompetences.backend.territoire.Commune commune;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_verification", nullable = false, length = 20)
    private StatutOrganisation statut = StatutOrganisation.EN_ATTENTE;

    @Column(name = "motif_statut", columnDefinition = "TEXT")
    private String motifStatut;
}