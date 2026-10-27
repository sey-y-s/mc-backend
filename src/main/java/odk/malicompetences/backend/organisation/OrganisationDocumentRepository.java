package odk.malicompetences.backend.organisation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrganisationDocumentRepository extends JpaRepository<OrganisationDocument, UUID> {

    List<OrganisationDocument> findByOrganisationIdOrderByDateDepotDesc(UUID organisationId);
}
