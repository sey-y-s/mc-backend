package odk.malicompetences.backend.validation;

import org.springframework.stereotype.Component;

@Component
public class ValidationMapper {

    public ValidationResponse toResponse(Validation validation) {
        return new ValidationResponse(
                validation.getId(),
                validation.getCitoyenCompetence().getId(),
                validation.getPreuve() != null
                        ? validation.getPreuve().getId()
                        : null,
                validation.getValideePar().getId(),
                validation.getCentre() != null
                        ? validation.getCentre().getId()
                        : null,
                validation.getType(),
                validation.getStatut(),
                validation.getDateValidation(),
                validation.getCommentaire(),
                validation.getDateExpiration()
        );
    }
}