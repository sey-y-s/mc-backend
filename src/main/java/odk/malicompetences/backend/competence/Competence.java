package odk.malicompetences.backend.competence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "competence")
@Getter
@Setter
@NoArgsConstructor
public class Competence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_referentiel_id")
    private UUID sourceReferentielId;

    @Column(unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 180)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean actif = true;

    @ManyToMany(mappedBy = "competences", fetch = FetchType.LAZY)
    private Set<Metier> metiers = new LinkedHashSet<>();

}
