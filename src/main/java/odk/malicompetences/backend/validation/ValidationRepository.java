package odk.malicompetences.backend.validation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ValidationRepository extends JpaRepository<Validation, UUID> {
}