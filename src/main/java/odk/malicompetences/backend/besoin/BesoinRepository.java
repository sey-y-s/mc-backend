package odk.malicompetences.backend.besoin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BesoinRepository extends JpaRepository<Besoin, UUID> {
}