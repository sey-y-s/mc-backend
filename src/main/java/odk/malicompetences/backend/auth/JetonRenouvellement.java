package odk.malicompetences.backend.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Jeton de renouvellement de session. Le jeton lui-même (aléatoire, opaque) n'est jamais conservé :
 * seule son empreinte SHA-256 l'est. Chaque jeton ne sert qu'une fois ; ses successeurs
 * appartiennent à la même « famille » afin de couper toute la chaîne en cas de vol détecté.
 */
@Entity
@Table(name = "jeton_renouvellement")
@Getter
@Setter
@NoArgsConstructor
public class JetonRenouvellement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "famille_id", nullable = false)
    private UUID familleId;

    @Column(nullable = false, length = 64, unique = true)
    private String empreinte;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe;

    @Column(name = "expire_le", nullable = false)
    private LocalDateTime expireLe;

    @Column(name = "utilise_le")
    private LocalDateTime utiliseLe;

    @Column(nullable = false)
    private boolean revoque = false;
}
