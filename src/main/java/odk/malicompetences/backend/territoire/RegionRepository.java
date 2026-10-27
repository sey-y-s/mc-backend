package odk.malicompetences.backend.territoire;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
    java.util.Optional<Region> findByCodeStable(String codeStable);
    java.util.List<Region> findAllByNiveau(NiveauTerritoire niveau);
    long countByNiveau(NiveauTerritoire niveau);
    java.util.List<Region> findAllByParent_IdAndNiveau(java.util.UUID parentId, NiveauTerritoire niveau);
    java.util.List<Region> findAllByNiveauNot(NiveauTerritoire niveau);
}