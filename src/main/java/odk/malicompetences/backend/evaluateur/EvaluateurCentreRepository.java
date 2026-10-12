package odk.malicompetences.backend.evaluateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface EvaluateurCentreRepository extends JpaRepository<EvaluateurCentre,UUID>{
 List<EvaluateurCentre> findByCitoyenIdAndActifTrue(UUID citoyenId);
 List<EvaluateurCentre> findByCentreIdAndActifTrue(UUID centreId);
 List<EvaluateurCentre> findByCentreId(UUID centreId);
 Optional<EvaluateurCentre> findByCitoyenIdAndCentreIdAndActifTrue(UUID citoyenId,UUID centreId);
 Optional<EvaluateurCentre> findByCitoyenIdAndCentreId(UUID citoyenId,UUID centreId);
 boolean existsByCitoyenIdAndCentreIdAndActifTrue(UUID citoyenId,UUID centreId);
}
