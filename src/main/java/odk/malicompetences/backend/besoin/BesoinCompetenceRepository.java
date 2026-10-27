package odk.malicompetences.backend.besoin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BesoinCompetenceRepository extends JpaRepository<BesoinCompetence, UUID> {
    List<BesoinCompetence> findByBesoinId(UUID besoinId);

    List<BesoinCompetence> findByBesoin_Statut(StatutBesoin statut);
    boolean existsByCompetence_Id(UUID competenceId);
}