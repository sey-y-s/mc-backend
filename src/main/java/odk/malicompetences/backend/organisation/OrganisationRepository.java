package odk.malicompetences.backend.organisation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {
    Optional<Organisation> findByUtilisateur_Id(UUID utilisateurId);
    boolean existsByUtilisateur_Id(UUID utilisateurId);
}
