package odk.malicompetences.backend.testnumerique;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List; import java.util.UUID;
public interface QuestionRepository extends JpaRepository<Question,UUID>{ List<Question> findByTestIdOrderByOrdreAsc(UUID testId); }
