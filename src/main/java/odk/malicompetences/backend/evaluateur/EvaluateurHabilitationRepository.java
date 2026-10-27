package odk.malicompetences.backend.evaluateur;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EvaluateurHabilitationRepository extends JpaRepository<EvaluateurHabilitation, UUID> {

    List<EvaluateurHabilitation> findByEvaluateurCentre_Id(UUID evaluateurCentreId);

    boolean existsByEvaluateurCentre_Id(UUID evaluateurCentreId);

    boolean existsByEvaluateurCentre_IdAndCompetence_Id(UUID evaluateurCentreId, UUID competenceId);

    void deleteByEvaluateurCentre_Id(UUID evaluateurCentreId);
}
