package odk.malicompetences.backend.preuve;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PreuveRepository
        extends JpaRepository<Preuve, UUID> {

    List<Preuve> findByCitoyenCompetenceId(
            UUID citoyenCompetenceId
    );
}