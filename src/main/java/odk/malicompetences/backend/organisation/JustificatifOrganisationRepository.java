package odk.malicompetences.backend.organisation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JustificatifOrganisationRepository extends JpaRepository<JustificatifOrganisation, UUID> {
    List<JustificatifOrganisation> findByOrganisation_IdOrderByIdDesc(UUID organisationId);
}
