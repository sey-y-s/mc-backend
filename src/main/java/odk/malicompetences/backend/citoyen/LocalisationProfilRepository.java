package odk.malicompetences.backend.citoyen;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LocalisationProfilRepository extends JpaRepository<LocalisationProfil, UUID> {
    Optional<LocalisationProfil> findByCitoyenId(UUID citoyenId);
}
