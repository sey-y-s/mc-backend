package odk.malicompetences.backend.competence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SecteurActiviteRepository extends JpaRepository<SecteurActivite, UUID> {

    Optional<SecteurActivite> findByCode(String code);

    List<SecteurActivite> findAllByActifOrderByNomAsc(boolean actif);
}
