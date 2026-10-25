package odk.malicompetences.backend.competence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetierRepository extends JpaRepository<Metier, UUID> {

    Optional<Metier> findByCode(String code);

    @Query("SELECT m FROM Metier m WHERE " +
            "(:secteurId IS NULL OR m.secteurActivite.id = :secteurId) AND " +
            "(:actif IS NULL OR m.actif = :actif) AND " +
            "(:q IS NULL OR LOWER(m.nom) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(m.code) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY m.nom")
    List<Metier> rechercher(
            @Param("secteurId") UUID secteurId,
            @Param("actif") Boolean actif,
            @Param("q") String q
    );
}
