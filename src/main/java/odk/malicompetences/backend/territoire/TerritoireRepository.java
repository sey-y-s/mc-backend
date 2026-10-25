package odk.malicompetences.backend.territoire;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TerritoireRepository extends JpaRepository<Territoire, UUID> {

    Optional<Territoire> findByCode(String code);


    boolean existsByCode(String code);

    List<Territoire> findAllByNiveau(NiveauTerritoire niveau);

    List<Territoire> findAllByParentId(UUID parentId);

    long countByNiveau(NiveauTerritoire niveau);

    @Query("SELECT t FROM Territoire t WHERE " +
            "(:niveau IS NULL OR t.niveau = :niveau) AND " +
            "(:parentId IS NULL OR (t.parent IS NOT NULL AND t.parent.id = :parentId)) AND " +
            "(:actif IS NULL OR t.actif = :actif) AND " +
            "(:q IS NULL OR LOWER(t.nom) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(t.code) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Territoire> rechercher(
            @Param("niveau") NiveauTerritoire niveau,
            @Param("parentId") UUID parentId,
            @Param("actif") Boolean actif,
            @Param("q") String q,
            Pageable pageable
    );
}
