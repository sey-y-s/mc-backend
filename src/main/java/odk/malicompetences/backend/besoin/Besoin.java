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
    private Organisation organisation; // <--- Génère setOrganisation() et getOrganisation()

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private StatutBesoin statut; // Adaptez le type d'enum si nécessaire

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private LocalDateTime dateCreation;
}