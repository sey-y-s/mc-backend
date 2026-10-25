package odk.malicompetences.backend.besoin;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BesoinRepository extends JpaRepository<Besoin, UUID> {
    List<Besoin> findAllByOrganisation_Id(UUID organisationId);
    long countByOrganisation_IdAndStatut(UUID organisationId, StatutBesoin statut);
}
