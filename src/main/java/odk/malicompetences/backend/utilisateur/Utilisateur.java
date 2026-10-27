package odk.malicompetences.backend.utilisateur;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.organisation.Organisation;

import java.time.LocalDateTime;
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

    @Column(nullable = false, unique = true, length = 30)
    private String telephone;

    @Column(unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    /** Date de vérification du téléphone par code OTP (nul = non vérifié). */
    @Column(name = "telephone_verifie_le")
    private LocalDateTime telephoneVerifieLe;

    /** Date de vérification de l'email par code OTP (nul = non vérifié). */
    @Column(name = "email_verifie_le")
    private LocalDateTime emailVerifieLe;

    @Column(name = "conditions_acceptees_le")
    private LocalDateTime conditionsAccepteesLe;

    /** Agent de terrain ayant ouvert ce compte (inscription assistée), sinon nul. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Utilisateur creePar;

    /** Date du consentement recueilli par l'agent lors d'une inscription assistée. */
    @Column(name = "consentement_recueilli_le")
    private LocalDateTime consentementRecueilliLe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Citoyen citoyen;

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Organisation organisation;

    /** Le compte est ouvert dès qu'au moins un des deux contacts est vérifié. */
    public boolean estVerifie() {
        return telephoneVerifieLe != null || emailVerifieLe != null;
    }

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}
