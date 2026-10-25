package odk.malicompetences.backend.testnumerique;

import com.fasterxml.jackson.databind.JsonNode; import jakarta.persistence.*; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter; import odk.malicompetences.backend.validation.DemandeValidation; import org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes; import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.UUID;
@Entity @Table(name="tentative_test") @Getter @Setter @NoArgsConstructor
public class TentativeTest {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="demande_validation_id",nullable=false) private DemandeValidation demande;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="test_id",nullable=false) private TestNumerique test;
 @Column(precision=5,scale=2) private BigDecimal score;
 @Column(name="seuil_applique",nullable=false,precision=5,scale=2) private BigDecimal seuilApplique;
 private Boolean reussi;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="reponses_json",columnDefinition="json") private JsonNode reponsesJson;
 @Column(name="commencee_le",nullable=false,updatable=false) private LocalDateTime commenceeLe;
 @Column(name="terminee_le") private LocalDateTime termineeLe;
 @PrePersist void prePersist(){if(commenceeLe==null)commenceeLe=LocalDateTime.now();}
}
