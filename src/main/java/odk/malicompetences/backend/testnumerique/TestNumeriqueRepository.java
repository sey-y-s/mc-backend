package odk.malicompetences.backend.testnumerique;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List; import java.util.UUID;
public interface TestNumeriqueRepository extends JpaRepository<TestNumerique,UUID>{ List<TestNumerique> findByCompetenceIdAndActifTrue(UUID competenceId); }
