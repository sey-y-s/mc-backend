package odk.malicompetences.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CodeVerificationRepository extends JpaRepository<CodeVerification, UUID> {

    Optional<CodeVerification> findFirstByUtilisateurIdAndCanalAndUtiliseLeIsNullOrderByCreeLeDesc(
            UUID utilisateurId, CanalVerification canal);

    Optional<CodeVerification> findFirstByUtilisateurIdAndCanalOrderByCreeLeDesc(
            UUID utilisateurId, CanalVerification canal);

    /** Invalide les anciens codes avant d'en émettre un nouveau. */
    @Modifying
    @Query("update CodeVerification c set c.utiliseLe = :maintenant "
            + "where c.utilisateurId = :id and c.canal = :canal and c.utiliseLe is null")
    int invaliderTous(@Param("id") UUID utilisateurId, @Param("canal") CanalVerification canal,
                      @Param("maintenant") LocalDateTime maintenant);
}
