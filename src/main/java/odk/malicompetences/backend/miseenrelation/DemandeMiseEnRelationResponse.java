package odk.malicompetences.backend.miseenrelation;

import java.time.LocalDateTime;
import java.util.UUID;

public record DemandeMiseEnRelationResponse(
        UUID id,
        UUID demandeurId,
        String demandeurNomComplet,
        UUID destinataireId,
        String destinataireNomComplet,
        UUID suiviBesoinTalentId,
        StatutContact statut,
        String message,
        LocalDateTime dateDemande,
        LocalDateTime dateReponse,
        /** Coordonnées de l'autre personne : renseignées seulement une fois la mise en relation acceptée. */
        Contact contact
) {
    public record Contact(String telephone, String email) {}
}