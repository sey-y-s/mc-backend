package odk.malicompetences.backend.centre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CentreRepository extends JpaRepository<Centre, UUID> {
    java.util.Optional<Centre> findFirstByAdministrateurId(UUID administrateurId);
}