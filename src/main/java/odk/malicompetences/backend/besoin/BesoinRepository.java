package odk.malicompetences.backend.besoin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BesoinRepository extends JpaRepository<Besoin, UUID> {
    long countByOrganisationIdAndStatut(UUID organisationId, StatutBesoin statut);
    List<Besoin> findByOrganisationId(UUID organisationId);
}