package odk.malicompetences.backend.citoyen;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import odk.malicompetences.backend.portfolio.PortfolioRealisation;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Disponibilite disponibilite = Disponibilite.DISPONIBLE;

    @OneToMany(
            mappedBy = "citoyen",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PortfolioRealisation> portfolioRealisations = new ArrayList<>();
}