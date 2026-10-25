package odk.malicompetences.backend.talent;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SuiviBesoinTalentRepository extends JpaRepository<SuiviBesoinTalent, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SuiviBesoinTalent s where s.id = :id")
    Optional<SuiviBesoinTalent> findByIdForUpdate(@Param("id") UUID id);
    List<SuiviBesoinTalent> findAllByBesoin_Id(UUID besoinId);

    boolean existsByBesoin_IdAndCitoyen_Id(
            UUID besoinId,
            UUID citoyenId
    );
}
