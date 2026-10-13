package odk.malicompetences.backend.citoyen;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.utilisateur.Utilisateur;
<<<<<<< HEAD
=======

>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
import odk.malicompetences.backend.portfolio.PortfolioRealisation;
import odk.malicompetences.backend.territoire.Commune;

import java.util.ArrayList;
import java.util.List;
<<<<<<< HEAD
=======

>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
import java.util.UUID;

@Entity
@Table(name = "citoyen")
<<<<<<< HEAD
=======

>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
@Getter
@Setter
@NoArgsConstructor
public class Citoyen {

    @Id
<<<<<<< HEAD
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Column(name = "code_passeport", nullable = false, unique = true, length = 14)
=======
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId                                                      // indique que le Citoyen utilise l'id de son Utilisateur.
    @JoinColumn(name = "id")
    private Utilisateur utilisateur;

    @Column(name = "code_passeport",nullable = false, unique = true, length = 14)
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
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

    @OneToMany(
            mappedBy = "citoyen",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PortfolioRealisation> portfolioRealisations = new ArrayList<>();
}