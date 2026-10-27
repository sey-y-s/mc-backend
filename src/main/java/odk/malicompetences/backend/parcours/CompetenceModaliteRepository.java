package odk.malicompetences.backend.parcours;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompetenceModaliteRepository extends JpaRepository<CompetenceModalite, UUID> {

    List<CompetenceModalite> findByCompetenceIdOrderByOrdreAsc(UUID competenceId);

    void deleteByCompetenceId(UUID competenceId);
}
