package odk.malicompetences.backend.validation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValidationRepository
        extends JpaRepository<Validation, UUID> {

    List<Validation> findByCentreIdAndStatut(UUID centreId, StatutValidation statut);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Validation v where v.id = :id")
    Optional<Validation> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByCitoyenCompetenceIdAndCentreIdAndStatut(UUID citoyenCompetenceId, UUID centreId, StatutValidation statut);

    List<Validation> findByCitoyenCompetenceId(
            UUID citoyenCompetenceId
    );

    Page<Validation> findByCitoyenCompetenceCitoyenId(
            UUID citoyenId,
            Pageable pageable
    );

    Page<Validation> findByCitoyenCompetenceCitoyenIdAndStatut(
            UUID citoyenId,
            StatutValidation statut,
            Pageable pageable
    );

    List<Validation> findByCitoyenCompetenceIdAndCitoyenCompetenceCitoyenId(
            UUID citoyenCompetenceId,
            UUID citoyenId
    );

    Optional<Validation> findByIdAndCitoyenCompetenceCitoyenId(
            UUID id,
            UUID citoyenId
    );
}