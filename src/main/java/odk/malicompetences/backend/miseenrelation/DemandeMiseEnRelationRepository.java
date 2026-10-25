package odk.malicompetences.backend.miseenrelation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DemandeMiseEnRelationRepository
        extends JpaRepository<DemandeMiseEnRelation, UUID> {

    boolean existsBySuiviBesoinTalent_IdAndStatut(UUID suiviId, StatutContact statut);

    boolean existsByDemandeur_IdAndDestinataire_IdAndSuiviBesoinTalentIsNullAndStatut(
            UUID demandeurId, UUID destinataireId, StatutContact statut);

    @Query("""
        select d from DemandeMiseEnRelation d
        where d.demandeur.id = :utilisateurId
           or d.destinataire.utilisateur.id = :utilisateurId
        order by d.dateDemande desc
        """)
    List<DemandeMiseEnRelation> findToutesPourUtilisateur(
            @Param("utilisateurId") UUID utilisateurId);

    @Query("""
        select max(d.dateReponse)
        from DemandeMiseEnRelation d
        where d.demandeur.id = :demandeurId
          and d.destinataire.id = :destinataireId
          and d.statut = :statut
        """)
    Optional<LocalDateTime> trouverDateDernierRefus(
            @Param("demandeurId") UUID demandeurId,
            @Param("destinataireId") UUID destinataireId,
            @Param("statut") StatutContact statut);

    boolean existsBySuiviBesoinTalent_Id(UUID id);
}
