package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.common.statut.StatutDemandeValidation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DemandeValidationDetailResponse(
        UUID id,
        UUID citoyenCompetenceId,
        String methode,
        UUID centreId,
        StatutDemandeValidation statut,
        String messageCitoyen,
        LocalDateTime creeeLe,
        LocalDateTime clotureeLe,
        List<DecisionValidationResponse> decisions
) {}
