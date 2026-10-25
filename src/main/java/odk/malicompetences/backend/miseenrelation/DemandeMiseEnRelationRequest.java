package odk.malicompetences.backend.miseenrelation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Une demande peut partir d'un suivi de besoin ou viser directement un profil talent.
 * destinataireId correspond à l'identifiant public renvoyé par la recherche de talents.
 */
public record DemandeMiseEnRelationRequest(
        UUID suiviBesoinTalentId,
        UUID destinataireId,
        @NotBlank(message = "Le message est obligatoire")
        @Size(max = 1000, message = "Le message ne doit pas dépasser 1000 caractères")
        String message
) {}
