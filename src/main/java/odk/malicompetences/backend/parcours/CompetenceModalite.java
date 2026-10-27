package odk.malicompetences.backend.parcours;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.competence.Competence;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/** Règle de validation configurée pour une compétence : une modalité autorisée et ses critères. */
@Entity
@Table(name = "competence_modalite",
        uniqueConstraints = @UniqueConstraint(name = "uk_competence_modalite", columnNames = {"competence_id", "modalite"}))
@Getter
@Setter
@NoArgsConstructor
public class CompetenceModalite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competence_id", nullable = false)
    private Competence competence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModaliteValidation modalite;

    @Column(nullable = false)
    private int ordre;

    /** Seuil de réussite en % (utilisé par le test numérique). */
    @Column(name = "seuil_reussite")
    private Integer seuilReussite;

    /** Types de pièces admissibles (valeurs de TypePreuve séparées par une virgule). */
    @Column(name = "pieces_admissibles", length = 300)
    private String piecesAdmissibles;

    /** Profils habilités à valider (jury, évaluateur de centre, maître artisan…). */
    @Column(name = "profils_habilites", length = 300)
    private String profilsHabilites;

    /** Critères d'évaluation (texte libre montré au candidat et à l'évaluateur). */
    @Column(length = 500)
    private String criteres;

    @Column(nullable = false)
    private boolean actif = true;
}
