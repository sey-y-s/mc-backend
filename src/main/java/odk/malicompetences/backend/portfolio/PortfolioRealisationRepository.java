package odk.malicompetences.backend.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PortfolioRealisationRepository extends JpaRepository<PortfolioRealisation, UUID> {
    List<PortfolioRealisation> findByCitoyenId(UUID citoyenId);
}