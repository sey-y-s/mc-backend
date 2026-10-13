package odk.malicompetences.backend.testnumerique;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ResultatTestRepository extends JpaRepository<ResultatTest, UUID> {
    List<ResultatTest> findByCitoyenIdOrderByDatePassageDesc(UUID citoyenId);
}
