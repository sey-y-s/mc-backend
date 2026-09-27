package odk.malicompetences.backend.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MediaRealisationRepository
        extends JpaRepository<MediaRealisation, UUID> {

    List<MediaRealisation>
    findByPortfolioRealisationId(UUID portfolioRealisationId);
}