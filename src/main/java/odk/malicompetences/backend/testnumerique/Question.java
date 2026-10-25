package odk.malicompetences.backend.testnumerique;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name="question", uniqueConstraints=@UniqueConstraint(name="uq_question_ordre", columnNames={"test_id", "ordre_affichage"}))
@Getter @Setter @NoArgsConstructor
public class Question {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="test_id", nullable=false) private TestNumerique test;
    @Column(name="enonce", nullable=false, columnDefinition="TEXT") private String texte;
    @Enumerated(EnumType.STRING) @Column(name="type_question", nullable=false, length=30) private TypeQuestion type;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="options_json", columnDefinition="json") private JsonNode optionsJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="reponse_attendue_json", nullable=false, columnDefinition="json") private JsonNode reponseAttendueJson;
    @Column(nullable=false, precision=7, scale=2) private BigDecimal ponderation = BigDecimal.ONE;
    @Column(name="ordre_affichage", nullable=false) private Integer ordre;
}
