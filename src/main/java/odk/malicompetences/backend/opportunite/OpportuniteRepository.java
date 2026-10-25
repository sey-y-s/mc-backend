package odk.malicompetences.backend.opportunite;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OpportuniteRepository extends JpaRepository<Opportunite, UUID> {

    @Query("""
            select o from Opportunite o
            where o.statut = :statut
              and (o.dateExpiration is null or o.dateExpiration >= :aujourdHui)
            order by o.publieeLe desc
            """)
    List<Opportunite> findVisibles(
            @Param("statut") StatutOpportunite statut,
            @Param("aujourdHui") LocalDate aujourdHui
    );

    @Query("""
            select o from Opportunite o
            where o.id = :id
              and o.statut = :statut
              and (o.dateExpiration is null or o.dateExpiration >= :aujourdHui)
            """)
    Optional<Opportunite> findVisibleById(
            @Param("id") UUID id,
            @Param("statut") StatutOpportunite statut,
            @Param("aujourdHui") LocalDate aujourdHui
    );
}
