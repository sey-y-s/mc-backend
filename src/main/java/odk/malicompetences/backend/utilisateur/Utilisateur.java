package odk.malicompetences.backend.utilisateur;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.common.statut.StatutCompte;
import odk.malicompetences.backend.organisation.Organisation;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@NoArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, length = 30)
    private String telephone;

    @Column(unique = true, length = 190)
    private String email;

    @Column(name = "mot_de_passe_hash")
    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCompte statut = StatutCompte.EN_ATTENTE;

    @Column(name = "telephone_verifie_le")
    private LocalDateTime telephoneVerifieLe;

    @Column(name = "email_verifie_le")
    private LocalDateTime emailVerifieLe;

    @Column(name = "conditions_acceptees_le")
    private LocalDateTime conditionsAccepteesLe;

    @Column(name = "cree_par_utilisateur_id")
    private UUID creeParUtilisateurId;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Column(name = "modifie_le")
    private LocalDateTime modifieLe;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "utilisateur_role",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Citoyen citoyen;

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Organisation organisation;

    @PrePersist
    protected void onCreate() {
        if (creeLe == null) {
            creeLe = LocalDateTime.now();
        }
        if (modifieLe == null) {
            modifieLe = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifieLe = LocalDateTime.now();
    }

}
