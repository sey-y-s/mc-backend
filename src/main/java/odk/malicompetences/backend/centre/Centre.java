package odk.malicompetences.backend.centre;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;
import odk.malicompetences.backend.utilisateur.Utilisateur;

@Entity
@Table(name = "centre")
@Getter
@Setter
@NoArgsConstructor
public class Centre {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String adresse;

    @Column(length = 30)
    private String telephone;
    
    /** Seul un centre ACTIF peut recevoir des demandes et évaluer (consignes V1.2). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCentre statut = StatutCentre.ACTIF;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrateur_id")
    private Utilisateur administrateur;

    /** Commune où se trouve le centre : sert à proposer au citoyen les centres proches de lui. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private odk.malicompetences.backend.territoire.Commune commune;
}