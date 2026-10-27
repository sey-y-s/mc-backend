package odk.malicompetences.backend.miseenrelation;

import jakarta.validation.constraints.NotNull;

/** Réponse du citoyen à une demande : ACCEPTEE ou REFUSEE. */
public record ReponseMiseEnRelationRequest(@NotNull StatutContact statut) {
}
