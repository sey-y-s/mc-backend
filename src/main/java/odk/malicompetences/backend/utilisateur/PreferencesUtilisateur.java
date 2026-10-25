package odk.malicompetences.backend.utilisateur;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "preference_utilisateur")
@Getter
@Setter
@NoArgsConstructor
public class PreferencesUtilisateur {
    @Id
    @Column(name = "utilisateur_id")
    private UUID utilisateurId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "mode_economie_donnees", nullable = false)
    private boolean lowDataMode;

    @Column(name = "notifications_actives", nullable = false)
    private boolean notificationsEnabled = true;
}
