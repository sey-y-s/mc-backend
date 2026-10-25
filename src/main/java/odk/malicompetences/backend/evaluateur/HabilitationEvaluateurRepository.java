package odk.malicompetences.backend.evaluateur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface HabilitationEvaluateurRepository extends JpaRepository<HabilitationEvaluateur, UUID> {
    List<HabilitationEvaluateur> findByEvaluateur_Citoyen_Utilisateur_Id(UUID utilisateurId);
    List<HabilitationEvaluateur> findByEvaluateur_Citoyen_Id(UUID citoyenId);
    List<HabilitationEvaluateur> findByCentre_Id(UUID centreId);
    List<HabilitationEvaluateur> findByEvaluateur_Citoyen_IdAndCentre_Id(UUID citoyenId, UUID centreId);
    @Query("select h from HabilitationEvaluateur h where h.evaluateur.id = :evaluateurId and h.competence.id = :competenceId and h.methode.code = :code")
    List<HabilitationEvaluateur> trouverPourEvaluation(@Param("evaluateurId") UUID evaluateurId, @Param("competenceId") UUID competenceId, @Param("code") String code);
}
