package odk.malicompetences.backend.validation;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.evaluateur.Evaluateur;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "evaluation_pratique") @Getter @Setter @NoArgsConstructor
public class EvaluationPratique {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "demande_validation_id", nullable = false) private DemandeValidation demande;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "evaluateur_id", nullable = false) private Evaluateur evaluateur;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "centre_id") private Centre centre;
    @Column(precision = 5, scale = 2) private BigDecimal score;
    @Column(columnDefinition = "TEXT") private String observations;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "grille_json", columnDefinition = "json") private JsonNode grilleJson;
    @Column(name = "realisee_le") private LocalDateTime realiseeLe;
    @PrePersist void avantCreation() { if (realiseeLe == null) realiseeLe = LocalDateTime.now(); }
}
