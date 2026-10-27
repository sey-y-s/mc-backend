package odk.malicompetences.backend.competence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.UUID;

public interface MetierRepository
        extends JpaRepository<Metier, UUID> {
    boolean existsBySecteurActiviteId(UUID id);

    /** Recherche pour la consultation publique : secteur et/ou fragment du nom, tous deux facultatifs. */
    @Query("""
            select m from Metier m
            where (:secteurId is null or m.secteurActivite.id = :secteurId)
              and (:q is null or lower(m.nom) like lower(concat('%', :q, '%')))
            order by m.nom
            """)
    List<Metier> rechercher(@Param("secteurId") UUID secteurId, @Param("q") String q);
}