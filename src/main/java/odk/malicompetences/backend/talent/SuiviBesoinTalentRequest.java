package odk.malicompetences.backend.talent;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record SuiviBesoinTalentRequest(
        @NotNull(message = "L'ID de l'organisation est obligatoire")
        UUID organisationId,

        @NotNull(message = "L'ID du besoin est obligatoire")
        UUID besoinId,

        @NotNull(message = "L'ID du citoyen (talent) est obligatoire")
        UUID citoyenId,

        @NotNull(message = "Le statut du suivi est obligatoire")
        StatutSuiviBesoinTalent statut,

        BigDecimal scoreCorrespondance,
        String detailsCorrespondance,
        String noteInterne
) {}