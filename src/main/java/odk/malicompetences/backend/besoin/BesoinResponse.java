package odk.malicompetences.backend.besoin;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BesoinResponse(
        UUID id,
        UUID organisationId,
        String organisationNom,
        String titre,
        String description,
        StatutBesoin statut,
        LocalDate dateLimite,
        LocalDateTime dateCreation,
        List<BesoinCompetenceResponse> competences
) {}