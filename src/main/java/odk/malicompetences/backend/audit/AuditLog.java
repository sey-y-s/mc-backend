package odk.malicompetences.backend.audit;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "journal_audit")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "acteur_id")
    private UUID acteurId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "type_ressource", nullable = false, length = 80)
    private String typeRessource;

    @Column(name = "ressource_id")
    private UUID ressourceId;

    @Column(name = "resultat", length = 24)
    private String resultat;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details_json", columnDefinition = "json")
    private JsonNode detailsJson;

    @Column(name = "adresse_ip", length = 45)
    private String adresseIp;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @jakarta.persistence.PrePersist
    protected void onCreate() { if (creeLe == null) creeLe = LocalDateTime.now(); }
}
