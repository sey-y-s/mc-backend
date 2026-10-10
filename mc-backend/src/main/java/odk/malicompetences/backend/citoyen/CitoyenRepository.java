package odk.malicompetences.backend.citoyen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CitoyenRepository extends JpaRepository<Citoyen, UUID> {
}