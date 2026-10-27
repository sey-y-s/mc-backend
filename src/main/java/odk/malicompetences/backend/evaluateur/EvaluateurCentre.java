package odk.malicompetences.backend.evaluateur;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evaluateur_centre",
        uniqueConstraints = @UniqueConstraint(name = "uk_evaluateur_centre", columnNames = {"citoyen_id", "centre_id"}))
@Getter
@Setter
@NoArgsConstructor
public class EvaluateurCentre {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citoyen citoyen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "centre_id", nullable = false)
    private Centre centre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "designe_par_id", nullable = false)
    private Utilisateur designePar;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(length = 200)
    private String specialite;

    /** Fin de validité de l'habilitation (nulle : sans échéance). */
    @Column(name = "date_fin_habilitation")
    private LocalDate dateFinHabilitation;

    @Column(name = "date_designation", nullable = false)
    private LocalDateTime dateDesignation;

    @PrePersist
    void prePersist() {
        if (dateDesignation == null) {
            dateDesignation = LocalDateTime.now();
        }
    }
}
