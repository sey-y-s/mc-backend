package odk.malicompetences.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface JetonRenouvellementRepository extends JpaRepository<JetonRenouvellement, UUID> {

    Optional<JetonRenouvellement> findByEmpreinte(String empreinte);

    /** Coupe toute une chaîne de renouvellement (vol détecté ou déconnexion). */
    @Modifying
    @Query("update JetonRenouvellement j set j.revoque = true where j.familleId = :famille and j.revoque = false")
    int revoquerFamille(@Param("famille") UUID familleId);

    /** Déconnecte tous les appareils d'un compte (changement ou réinitialisation du mot de passe). */
    @Modifying
    @Query("update JetonRenouvellement j set j.revoque = true where j.utilisateurId = :utilisateur and j.revoque = false")
    int revoquerTous(@Param("utilisateur") UUID utilisateurId);

    @Modifying
    @Query("delete from JetonRenouvellement j where j.expireLe < :limite")
    int supprimerExpires(@Param("limite") LocalDateTime limite);
}
