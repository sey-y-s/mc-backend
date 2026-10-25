package odk.malicompetences.backend.competence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetenceRepository extends JpaRepository<Competence, UUID> {

    Optional<Competence> findByCode(String code);

    @Query("SELECT DISTINCT c FROM Competence c " +
            "LEFT JOIN c.metiers m " +
            "WHERE (:actif IS NULL OR c.actif = :actif) " +
            "AND (:metierId IS NULL OR m.id = :metierId) " +
            "AND (:secteurId IS NULL OR m.secteurActivite.id = :secteurId) " +
            "AND (:q IS NULL OR LOWER(c.nom) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY c.nom")
    List<Competence> rechercher(
            @Param("metierId") UUID metierId,
            @Param("secteurId") UUID secteurId,
            @Param("actif") Boolean actif,
            @Param("q") String q
    );
}
