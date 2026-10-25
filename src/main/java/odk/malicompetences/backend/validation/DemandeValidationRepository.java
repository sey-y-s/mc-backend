package odk.malicompetences.backend.validation;

import jakarta.persistence.LockModeType;
import odk.malicompetences.backend.common.statut.StatutDemandeValidation;
import odk.malicompetences.backend.common.statut.MethodeValidationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DemandeValidationRepository extends JpaRepository<DemandeValidation, UUID> {
    boolean existsByCitoyenCompetence_Id(UUID citoyenCompetenceId);

    List<DemandeValidation> findByCitoyenCompetenceCitoyenIdOrderByCreeeLeDesc(UUID citoyenId);
    @Query("select (count(d) > 0) from DemandeValidation d where d.citoyenCompetence.id = :ccId and d.methode.code = :code and d.statut in :statuts")
    boolean existeDemandeActive(@Param("ccId") UUID ccId, @Param("code") String code, @Param("statuts") Collection<StatutDemandeValidation> statuts);
    @Query("""
            select d from DemandeValidation d
            left join fetch d.citoyenCompetence cc
            left join fetch cc.citoyen c
            left join fetch c.utilisateur
            left join fetch cc.competence
            left join fetch d.methode
            where d.id = :id
            """)
    Optional<DemandeValidation> findByIdWithRelations(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DemandeValidation d where d.id = :id")
    Optional<DemandeValidation> findByIdForUpdate(@Param("id") UUID id);
    @Query("select d from DemandeValidation d where d.evaluateurAssigneId = :evaluateurId order by d.creeeLe desc")
    List<DemandeValidation> findAssignees(@Param("evaluateurId") UUID evaluateurId);

    @Query("""
            select d from DemandeValidation d
            where (:statut is null or d.statut = :statut)
              and (:methodeCode is null or d.methode.code = :methodeCode)
              and (:competenceId is null or d.citoyenCompetence.competence.id = :competenceId)
              and (:evaluateurId is null or d.evaluateurAssigneId = :evaluateurId)
            order by d.creeeLe desc
            """)
    List<DemandeValidation> rechercher(@Param("statut") StatutDemandeValidation statut,
                                      @Param("methodeCode") String methodeCode,
                                      @Param("competenceId") UUID competenceId,
                                      @Param("evaluateurId") UUID evaluateurId);

    List<DemandeValidation> findByCitoyenCompetence_IdOrderByCreeeLeDesc(UUID citoyenCompetenceId);
}
