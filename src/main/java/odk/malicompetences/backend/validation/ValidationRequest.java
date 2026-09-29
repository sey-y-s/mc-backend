package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.validation.TypeValidation;

import java.util.UUID;

public record ValidationRequest(
        UUID preuveId,
        UUID valideeParId,
        UUID centreId,
        TypeValidation type,
        String commentaire
) {
}