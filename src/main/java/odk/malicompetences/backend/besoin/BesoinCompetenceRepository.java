package odk.malicompetences.backend.besoin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BesoinCompetenceRepository extends JpaRepository<BesoinCompetence, UUID> {
}