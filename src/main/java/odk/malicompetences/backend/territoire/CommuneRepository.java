package odk.malicompetences.backend.territoire;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommuneRepository extends JpaRepository<Commune, UUID> {
}