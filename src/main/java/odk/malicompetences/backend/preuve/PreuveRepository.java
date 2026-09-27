package odk.malicompetences.backend.preuve;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PreuveRepository extends JpaRepository<Preuve, UUID> {
}