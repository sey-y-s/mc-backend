package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.statut.UsageCodeVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CodeVerificationRepository extends JpaRepository<CodeVerification, UUID> {
    Optional<CodeVerification> findTopByDestinataireAndUsageCodeOrderByCreeLeDesc(
            String destinataire,
            UsageCodeVerification usageCode
    );
}

