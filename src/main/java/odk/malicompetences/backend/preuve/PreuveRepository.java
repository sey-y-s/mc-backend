package odk.malicompetences.backend.preuve;
import odk.malicompetences.backend.common.statut.StatutPreuve;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface PreuveRepository extends JpaRepository<Preuve, UUID> {
    List<Preuve> findByCitoyenCompetenceIdAndStatutNot(UUID citoyenCompetenceId, StatutPreuve statut);
    List<Preuve> findByCitoyenCompetenceCitoyenIdAndStatutNot(UUID citoyenId, StatutPreuve statut);
}
