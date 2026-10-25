package odk.malicompetences.backend.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findTop100ByActeurIdOrderByCreeLeDesc(UUID acteurId);
    List<AuditLog> findTop100ByTypeRessourceAndRessourceIdOrderByCreeLeDesc(String typeRessource, UUID ressourceId);
}
