package odk.malicompetences.backend.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PortfolioRealisationRepository
        extends JpaRepository<PortfolioRealisation, UUID> {
}