package odk.malicompetences.backend.preuve;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PreuveRepository
        extends JpaRepository<Preuve, UUID> {

    List<Preuve> findByCitoyenCompetenceId(
            UUID citoyenCompetenceId
    );

    List<Preuve> findByCitoyenCompetenceCitoyenId(UUID citoyenId);

    @Query("""
            select case when count(p) > 0 then true else false end
            from Preuve p
            where p.fichierUrl = :url
              and p.citoyenCompetence.citoyen.id = :citoyenId
            """)
    boolean existsByUrlAndCitoyenId(
            @Param("url") String url,
            @Param("citoyenId") UUID citoyenId
    );
}