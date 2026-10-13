package odk.malicompetences.backend.opportunite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategorieOpportuniteRepository
        extends JpaRepository<CategorieOpportunite, UUID> {
}