package odk.malicompetences.backend.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.common.statut.CanalVerification;
import odk.malicompetences.backend.common.statut.UsageCodeVerification;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "code_verification")
@Getter
@Setter
@NoArgsConstructor
public class CodeVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "utilisateur_id")
    private UUID utilisateurId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CanalVerification canal;

    @Column(nullable = false, length = 190)
    private String destinataire;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_code", nullable = false, length = 40)
    private UsageCodeVerification usageCode;

    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "expire_le", nullable = false)
    private LocalDateTime expireLe;

    @Column(name = "utilise_le")
    private LocalDateTime utiliseLe;

    @Column(nullable = false)
    private int tentatives = 0;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @PrePersist
    protected void onCreate() {
        if (creeLe == null) {
            creeLe = LocalDateTime.now();
        }
    }
}

