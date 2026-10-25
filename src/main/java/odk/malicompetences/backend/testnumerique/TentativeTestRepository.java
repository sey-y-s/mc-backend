package odk.malicompetences.backend.testnumerique;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TentativeTestRepository extends JpaRepository<TentativeTest,UUID>{ List<TentativeTest> findByDemandeIdOrderByCommenceeLeDesc(UUID demandeId); boolean existsByDemandeIdAndTestIdAndTermineeLeIsNotNull(UUID demandeId,UUID testId); boolean existsByTestIdAndTermineeLeIsNotNull(UUID testId); }
