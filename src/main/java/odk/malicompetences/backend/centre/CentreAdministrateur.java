package odk.malicompetences.backend.centre;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "centre_administrateur")
@Getter
@Setter
@NoArgsConstructor
public class CentreAdministrateur {

    @Id
    @Column(name = "utilisateur_id")
    private UUID utilisateurId;

    @Column(name = "centre_id", nullable = false)
    private UUID centreId;

    @Column(name = "attribue_par_utilisateur_id")
    private UUID attribueParUtilisateurId;

    @Column(name = "attribue_le")
    private LocalDateTime attribueLe;

    public CentreAdministrateur(UUID utilisateurId, UUID centreId) {
        this.utilisateurId = utilisateurId;
        this.centreId = centreId;
        this.attribueLe = LocalDateTime.now();
    }
}

