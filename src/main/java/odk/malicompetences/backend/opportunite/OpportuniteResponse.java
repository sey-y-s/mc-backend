package odk.malicompetences.backend.opportunite;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record OpportuniteResponse(
        UUID id,
        UUID categorieId,
        String categorieNom,
        String titre,
        String description,
        StatutOpportunite statut,
        TypeOpportunite type,
        LocalDateTime datePublication,
        LocalDate dateExpiration
) {}