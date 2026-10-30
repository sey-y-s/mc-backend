package odk.malicompetences.backend.miseenrelation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DemandeMiseEnRelationRepository
        extends JpaRepository<DemandeMiseEnRelation, UUID> {

    List<DemandeMiseEnRelation> findByDestinataire_IdOrderByDateDemandeDesc(UUID destinataireId);

        List<DemandeMiseEnRelation> findByDemandeur_IdOrderByDateDemandeDesc(UUID demandeurId);

    /** Reçues et envoyées : un citoyen peut contacter un autre talent comme être contacté. */
    List<DemandeMiseEnRelation> findByDemandeur_IdOrDestinataire_IdOrderByDateDemandeDesc(UUID demandeurId, UUID destinataireId);

        @org.springframework.data.jpa.repository.Query("select d.destinataire.id from DemandeMiseEnRelation d "
            + "where d.statut = odk.malicompetences.backend.miseenrelation.StatutContact.ACCEPTEE and d.demandeur.id = :moi")
    List<UUID> destinatairesAcceptes(@org.springframework.data.repository.query.Param("moi") UUID moi);

    @org.springframework.data.jpa.repository.Query("select d.demandeur.id from DemandeMiseEnRelation d "
            + "where d.statut = odk.malicompetences.backend.miseenrelation.StatutContact.ACCEPTEE and d.destinataire.id = :moi")
    List<UUID> demandeursAcceptes(@org.springframework.data.repository.query.Param("moi") UUID moi);

    /** Personnes avec qui [moi] a une mise en relation acceptée (dans un sens ou dans l'autre). */
    default java.util.Set<UUID> contactsEtablis(UUID moi) {
        java.util.Set<UUID> ids = new java.util.HashSet<>(destinatairesAcceptes(moi));
        ids.addAll(demandeursAcceptes(moi));
        return ids;
    }

    boolean existsBySuiviBesoinTalent_IdAndStatut(UUID suiviId, StatutContact statut);

    boolean existsByDemandeur_IdAndDestinataire_IdAndStatut(UUID demandeurId, UUID destinataireId, StatutContact statut);

    java.util.Optional<DemandeMiseEnRelation> findFirstByDemandeur_IdAndDestinataire_IdAndStatutOrderByDateReponseDesc(
            UUID demandeurId, UUID destinataireId, StatutContact statut);
}