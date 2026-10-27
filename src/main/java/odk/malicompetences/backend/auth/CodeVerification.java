package odk.malicompetences.backend.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Code de vérification à usage unique d'un contact (jamais stocké en clair). */
@Entity
@Table(name = "code_verification")
@Getter
@Setter
@NoArgsConstructor
public class CodeVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalVerification canal;

    @Column(name = "code_hash", nullable = false, length = 100)
    private String codeHash;

    @Column(name = "expire_le", nullable = false)
    private LocalDateTime expireLe;

    @Column(name = "utilise_le")
    private LocalDateTime utiliseLe;

    @Column(nullable = false)
    private int tentatives = 0;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe;

    @PrePersist
    protected void onCreate() {
        creeLe = LocalDateTime.now();
    }
}
