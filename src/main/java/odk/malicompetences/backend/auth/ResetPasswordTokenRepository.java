package odk.malicompetences.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, UUID> {

    Optional<ResetPasswordToken> findFirstByUtilisateurIdAndUtiliseFalseOrderByDateCreationDesc(UUID utilisateurId);

    /** Invalide les anciens codes avant d'en émettre un nouveau. */
    @Modifying
    @Query("update ResetPasswordToken t set t.utilise = true where t.utilisateurId = :id and t.utilise = false")
    int invaliderTous(@Param("id") UUID utilisateurId);
}
