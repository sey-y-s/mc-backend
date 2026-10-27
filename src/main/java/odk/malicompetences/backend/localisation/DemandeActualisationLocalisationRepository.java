package odk.malicompetences.backend.localisation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DemandeActualisationLocalisationRepository extends JpaRepository<DemandeActualisationLocalisation, UUID> {

    Optional<DemandeActualisationLocalisation> findFirstByCitoyen_IdAndStatut(UUID citoyenId, StatutActualisation statut);

    Optional<DemandeActualisationLocalisation> findFirstByCitoyen_IdAndStatutOrderByDateReponseDesc(UUID citoyenId, StatutActualisation statut);

    List<DemandeActualisationLocalisation> findByStatut(StatutActualisation statut);
}
