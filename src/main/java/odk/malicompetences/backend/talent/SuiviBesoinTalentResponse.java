package odk.malicompetences.backend.talent;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SuiviBesoinTalentResponse(
        UUID id,
        UUID organisationId,
        UUID besoinId,
        String besoinTitre,
        UUID citoyenId,
        String citoyenNomComplet,
        StatutSuiviBesoinTalent statut,
        BigDecimal scoreCorrespondance,
        String detailsCorrespondance,
        String noteInterne,
        LocalDateTime dateCreation,
        LocalDateTime dateMiseAJour
) {}