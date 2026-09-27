package odk.malicompetences.backend.opportunite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OpportuniteRepository
        extends JpaRepository<Opportunite, UUID> {
}