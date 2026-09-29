package odk.malicompetences.backend.miseenrelation;

import java.time.LocalDateTime;
import java.util.UUID;

public record DemandeMiseEnRelationResponse(
        UUID id,
        UUID demandeurId,
        String demandeurNomComplet,
        UUID destinataireId,
        String destinataireNomComplet,
        UUID suiviTalentId,
        StatutContact statut,
        String message,
        LocalDateTime dateDemande,
        LocalDateTime dateReponse
) {}