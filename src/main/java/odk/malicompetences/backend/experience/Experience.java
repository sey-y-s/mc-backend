package odk.malicompetences.backend.experience;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity @Table(name="experience") @Getter @Setter @NoArgsConstructor
public class Experience {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="citoyen_id", nullable=false) private Citoyen citoyen;
    @Column(name="intitule", nullable=false, length=180) private String titre;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="date_debut") private LocalDate dateDebut;
    @Column(name="date_fin") private LocalDate dateFin;
    @Column(name="en_cours", nullable=false) private boolean enCours;
    @Column(name="employeur_ou_client", length=180) private String entreprise;
    @ManyToMany
    @JoinTable(name="experience_competence", joinColumns=@JoinColumn(name="experience_id"), inverseJoinColumns=@JoinColumn(name="citoyen_competence_id"))
    private Set<CitoyenCompetence> competences = new HashSet<>();
}
