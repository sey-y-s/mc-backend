package odk.malicompetences.backend.talent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SuiviBesoinTalentRepository extends JpaRepository<SuiviBesoinTalent, UUID> {
}