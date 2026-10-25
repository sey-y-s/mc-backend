package odk.malicompetences.backend.besoin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BesoinZoneRepository extends JpaRepository<BesoinZone, UUID> {
    List<BesoinZone> findByBesoin_IdOrderByIdAsc(UUID besoinId);
    void deleteByBesoin_Id(UUID besoinId);
}
