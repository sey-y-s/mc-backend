package odk.malicompetences.backend.territoire;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommuneRepository extends JpaRepository<Commune, UUID> {

    Optional<Commune> findByCode(String code);

    boolean existsByCode(String code);

    List<Commune> findAllByTerritoireId(UUID territoireId);

    @Query("SELECT c FROM Commune c WHERE " +
            "(:territoireId IS NULL OR c.territoire.id = :territoireId) AND " +
            "(:actif IS NULL OR c.actif = :actif) AND " +
            "(:q IS NULL OR LOWER(c.nom) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Commune> rechercher(
            @Param("territoireId") UUID territoireId,
            @Param("actif") Boolean actif,
            @Param("q") String q,
            Pageable pageable
    );

    @Query("SELECT c FROM Commune c WHERE c.territoire.id = :territoireId OR (c.territoire.parent IS NOT NULL AND c.territoire.parent.id = :territoireId)")
    List<Commune> findCommunesRecursive(@Param("territoireId") UUID territoireId);
}
