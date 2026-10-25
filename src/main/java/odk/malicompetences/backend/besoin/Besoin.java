package odk.malicompetences.backend.besoin;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.organisation.Organisation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "besoin")
@Getter
@Setter
@NoArgsConstructor
public class Besoin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false, length = 180)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutBesoin statut = StatutBesoin.BROUILLON;

    @Column(name = "date_limite")
    private LocalDate dateLimite;

    @Column(name = "publie_le")
    private LocalDateTime publieLe;

    @Column(name = "cree_le", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "modifie_le", nullable = false, insertable = false, updatable = false)
    private LocalDateTime modifieLe;

    @PrePersist
    void initialiserHorodatages() {
        LocalDateTime maintenant = LocalDateTime.now();
        if (dateCreation == null) dateCreation = maintenant;
        if (modifieLe == null) modifieLe = maintenant;
    }

    @PreUpdate
    void actualiserHorodatageModification() {
        modifieLe = LocalDateTime.now();
    }

}