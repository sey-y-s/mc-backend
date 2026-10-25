package odk.malicompetences.backend.centre;

import org.springframework.data.jpa.repository.JpaRepository;
import odk.malicompetences.backend.common.statut.StatutCentre;

import java.util.List;
import java.util.UUID;

public interface CentreRepository extends JpaRepository<Centre, UUID> {

    List<Centre> findByStatut(StatutCentre statut);
}
