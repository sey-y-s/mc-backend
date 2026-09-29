package odk.malicompetences.backend.miseenrelation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DemandeMiseEnRelationRequest(
        @NotNull(message = "L'ID du demandeur est obligatoire")
        UUID demandeurId,

        @NotNull(message = "L'ID du destinataire (citoyen) est obligatoire")
        UUID destinataireId,

        UUID suiviTalentId,

        @NotNull(message = "Le statut du contact est obligatoire")
        StatutContact statut,

        @NotBlank(message = "Le message est obligatoire")
        @Size(max = 1000, message = "Le message ne doit pas dépasser 1000 caractères")
        String message
) {}