package odk.malicompetences.backend.miseenrelation;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConsultationProfilRepository extends JpaRepository<ConsultationProfil, UUID> {
}
