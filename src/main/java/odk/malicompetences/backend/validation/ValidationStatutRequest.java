package odk.malicompetences.backend.validation;

public record ValidationStatutRequest(
        StatutValidation statut,
        String commentaire
) {}