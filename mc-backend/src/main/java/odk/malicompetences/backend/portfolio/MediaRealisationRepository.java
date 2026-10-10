package odk.malicompetences.backend.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MediaRealisationRepository
        extends JpaRepository<MediaRealisation, UUID> {

    List<MediaRealisation>
    findByPortfolioRealisationId(UUID portfolioRealisationId);

    @Query("""
            select case when count(m) > 0 then true else false end
            from MediaRealisation m
            where m.url = :url
              and m.portfolioRealisation.citoyen.id = :citoyenId
            """)
    boolean existsByUrlAndCitoyenId(
            @Param("url") String url,
            @Param("citoyenId") UUID citoyenId
    );
}