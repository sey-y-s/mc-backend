package odk.malicompetences.backend.besoin;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BesoinCompetenceRepository extends JpaRepository<BesoinCompetence, UUID> {
    List<BesoinCompetence> findByBesoin_Id(UUID besoinId);
    boolean existsByCompetence_Id(UUID competenceId);
    java.util.Optional<BesoinCompetence> findByBesoin_IdAndCompetence_Id(UUID besoinId, UUID competenceId);
    boolean existsByBesoin_IdAndCompetence_Id(UUID besoinId, UUID competenceId);
}