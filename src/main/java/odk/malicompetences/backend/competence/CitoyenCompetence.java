package odk.malicompetences.backend.competence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.common.statut.SituationCompetence;
import odk.malicompetences.backend.common.statut.StatutValidationCompetence;
import odk.malicompetences.backend.experience.Experience;
import odk.malicompetences.backend.preuve.Preuve;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity @Table(name="citoyen_competence", uniqueConstraints=@UniqueConstraint(name="uq_citoyen_competence", columnNames={"citoyen_id","competence_id"}))
@Getter @Setter @NoArgsConstructor
public class CitoyenCompetence {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="citoyen_id", nullable=false) private Citoyen citoyen;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="competence_id", nullable=false) private Competence competence;
    @Enumerated(EnumType.STRING) @Column(name="niveau_declare", length=20) private NiveauCompetence niveauDeclare;
    @Enumerated(EnumType.STRING) @Column(name="niveau_valide", length=20) private NiveauCompetence niveauValide;
    @Enumerated(EnumType.STRING) @Column(name="statut_validation", nullable=false, length=20) private StatutValidationCompetence statutValidation = StatutValidationCompetence.NON_VERIFIEE;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private SituationCompetence situation = SituationCompetence.PRATIQUEE;
    @Column(nullable=false) private boolean disponibilite;
    @Column(name="derniere_methode_id") private UUID derniereMethodeId;
    @Column(name="validee_le") private LocalDateTime valideeLe;
    @Column(name="valide_jusqu_au") private LocalDate valideJusquAu;
    @Column(name="declaree_le", nullable=false, updatable=false) private LocalDateTime declareeLe;
    @Column(name="modifiee_le", nullable=false) private LocalDateTime modifieeLe;
    @PrePersist
    void initialiserDates() {
        LocalDateTime maintenant = LocalDateTime.now();
        if (declareeLe == null) declareeLe = maintenant;
        if (modifieeLe == null) modifieeLe = maintenant;
    }

    @PreUpdate
    void actualiserDateModification() {
        modifieeLe = LocalDateTime.now();
    }

    @ManyToMany(mappedBy="competences") private Set<Experience> experiences = new HashSet<>();
    @OneToMany(mappedBy="citoyenCompetence") private List<Preuve> preuves = new ArrayList<>();

}
