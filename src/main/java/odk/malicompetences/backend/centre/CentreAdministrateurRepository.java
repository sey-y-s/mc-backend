package odk.malicompetences.backend.centre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CentreAdministrateurRepository extends JpaRepository<CentreAdministrateur, UUID> {
    Optional<CentreAdministrateur> findByUtilisateurId(UUID utilisateurId);
    List<CentreAdministrateur> findAllByCentreId(UUID centreId);
    boolean existsByUtilisateurId(UUID utilisateurId);
}

