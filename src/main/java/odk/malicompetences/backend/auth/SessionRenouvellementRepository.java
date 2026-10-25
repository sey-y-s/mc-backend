package odk.malicompetences.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SessionRenouvellementRepository extends JpaRepository<SessionRenouvellement, UUID> {

    Optional<SessionRenouvellement> findByJetonHash(String jetonHash);

    @Modifying
    @Query("update SessionRenouvellement s set s.revoqueLe = :date where s.utilisateurId = :utilisateurId and s.revoqueLe is null")
    int revoquerToutes(@Param("utilisateurId") UUID utilisateurId, @Param("date") LocalDateTime date);
}
