package odk.malicompetences.backend.opportunite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidatureOpportuniteRepository extends JpaRepository<CandidatureOpportunite, UUID> {

    long countByOpportuniteId(UUID opportuniteId);

    boolean existsByOpportuniteIdAndCitoyenId(UUID opportuniteId, UUID citoyenId);

    Optional<CandidatureOpportunite> findByOpportuniteIdAndCitoyenId(UUID opportuniteId, UUID citoyenId);

    List<CandidatureOpportunite> findByOpportuniteIdOrderByLocaleDescDateCandidatureAsc(UUID opportuniteId);

    List<CandidatureOpportunite> findByCitoyenIdOrderByDateCandidatureDesc(UUID citoyenId);
}
