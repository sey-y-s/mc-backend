package odk.malicompetences.backend.testnumerique;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PropositionReponseRepository extends JpaRepository<PropositionReponse, UUID> {
    List<PropositionReponse> findByQuestionId(UUID questionId);
}
