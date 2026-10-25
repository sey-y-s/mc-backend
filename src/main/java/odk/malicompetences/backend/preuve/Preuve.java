package odk.malicompetences.backend.preuve;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.common.statut.StatutPreuve;
import odk.malicompetences.backend.common.statut.TypePreuveCitoyen;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name="preuve") @Getter @Setter @NoArgsConstructor
public class Preuve {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="citoyen_competence_id", nullable=false) private CitoyenCompetence citoyenCompetence;
    @Enumerated(EnumType.STRING) @Column(name="type_preuve", nullable=false, length=30) private TypePreuveCitoyen type;
    @Column(length=180) private String titre;
    @Column(name="cle_stockage_prive", length=500) private String cleStockagePrive;
    @Column(name="reference_document", length=180) private String referenceDocument;
    @Column(columnDefinition="TEXT") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private StatutPreuve statut=StatutPreuve.EN_ATTENTE;
    @Column(name="visible_profil", nullable=false) private boolean visibleProfil;
    @Column(name="deposee_le", nullable=false, updatable=false) private LocalDateTime deposeeLe;
    @PrePersist protected void initialiserDate() { if (deposeeLe == null) deposeeLe = LocalDateTime.now(); }
}
