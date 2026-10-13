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
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutOrganisation statut = StatutOrganisation.EN_ATTENTE;
}