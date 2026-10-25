package odk.malicompetences.backend.validation;

import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface CompetenceMethodeValidationRepository extends Repository<MethodeValidation, UUID> {
    @Query(value = "select count(*) > 0 from competence_methode_validation where competence_id = :competenceId and methode_id = :methodeId and active = true", nativeQuery = true)
    boolean estAutorisee(@Param("competenceId") UUID competenceId, @Param("methodeId") UUID methodeId);
}
