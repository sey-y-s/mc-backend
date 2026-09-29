package odk.malicompetences.backend.talent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SuiviTalentResponse(
        UUID id,
        UUID organisationId,
        UUID besoinId,
        String besoinTitre,
        UUID citoyenId,
        String citoyenNomComplet,
        StatutSuiviTalent statut,
        BigDecimal scoreCorrespondance,
        String detailsCorrespondance,
        String noteInterne,
        LocalDateTime dateCreation,
        LocalDateTime dateMiseAJour
) {}