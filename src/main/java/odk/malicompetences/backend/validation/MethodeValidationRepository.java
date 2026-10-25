package odk.malicompetences.backend.validation;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional; import java.util.UUID;
public interface MethodeValidationRepository extends JpaRepository<MethodeValidation,UUID>{ Optional<MethodeValidation> findByCodeAndActiveTrue(String code); }
