package odk.malicompetences.backend.evaluateur;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional; import java.util.UUID;
public interface EvaluateurRepository extends JpaRepository<Evaluateur,UUID>{ Optional<Evaluateur> findByCitoyenId(UUID citoyenId); }
