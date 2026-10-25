package odk.malicompetences.backend.besoin;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BesoinRequest(
        @NotNull(message = "L'ID de l'organisation est obligatoire")
        UUID organisationId,

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
        String titre,

        String description,

        @NotNull(message = "Le statut du besoin est obligatoire")
        StatutBesoin statut,

        LocalDate dateLimite,

        List<@Valid BesoinCompetenceRequest> competences
) {}