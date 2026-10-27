package odk.malicompetences.backend.miseenrelation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DemandeMiseEnRelationRepository
        extends JpaRepository<DemandeMiseEnRelation, UUID> {

    List<DemandeMiseEnRelation> findByDestinataire_IdOrderByDateDemandeDesc(UUID destinataireId);

    List<DemandeMiseEnRelation> findByDemandeur_IdOrderByDateDemandeDesc(UUID demandeurId);

    boolean existsBySuiviBesoinTalent_IdAndStatut(UUID suiviId, StatutContact statut);

    boolean existsByDemandeur_IdAndDestinataire_IdAndStatut(UUID demandeurId, UUID destinataireId, StatutContact statut);

    java.util.Optional<DemandeMiseEnRelation> findFirstByDemandeur_IdAndDestinataire_IdAndStatutOrderByDateReponseDesc(
            UUID demandeurId, UUID destinataireId, StatutContact statut);
}