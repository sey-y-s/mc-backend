package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.preuve.Preuve;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class ValidationMapper {

    public Validation toEntity(ValidationRequest request, CitoyenCompetence citoyenCompetence, Preuve preuve, Utilisateur valideePar, Centre centre) {
        if (request == null) return null;

        Validation validation = new Validation();
        validation.setCitoyenCompetence(citoyenCompetence);
        validation.setPreuve(preuve);
        validation.setValideePar(valideePar);
        validation.setCentre(centre);
        validation.setType(request.type());
        // Une nouvelle validation reste en attente jusqu'à la décision d'un évaluateur autorisé.
        validation.setStatut(StatutValidation.EN_ATTENTE);
        validation.setCommentaire(request.commentaire());
        validation.setDateExpiration(request.dateExpiration());

        return validation;
    }

    public void updateEntity(ValidationRequest request, Validation validation, CitoyenCompetence citoyenCompetence, Preuve preuve, Centre centre) {
        if (request != null && validation != null) {
            if (citoyenCompetence != null) {
                validation.setCitoyenCompetence(citoyenCompetence);
            }
            validation.setPreuve(preuve); // Peut être null
            validation.setCentre(centre); // Peut être null
            validation.setType(request.type());
            validation.setCommentaire(request.commentaire());
            validation.setDateExpiration(request.dateExpiration());

        }
    }

    public ValidationResponse toResponse(Validation validation) {
        if (validation == null) return null;

        UUID citoyenCompetenceId = validation.getCitoyenCompetence() != null ? validation.getCitoyenCompetence().getId() : null;
        UUID preuveId = validation.getPreuve() != null ? validation.getPreuve().getId() : null;
        UUID valideeParId = validation.getValideePar() != null ? validation.getValideePar().getId() : null;
        String valideeParTelephone = validation.getValideePar() != null ? validation.getValideePar().getTelephone() : null;
        UUID centreId = validation.getCentre() != null ? validation.getCentre().getId() : null;
        String centreNom = validation.getCentre() != null ? validation.getCentre().getNom() : null;

        return new ValidationResponse(
                validation.getId(),
                citoyenCompetenceId,
                preuveId,
                valideeParId,
                valideeParTelephone,
                centreId,
                centreNom,
                validation.getType(),
                validation.getStatut(),
                validation.getDateValidation(),
                validation.getCommentaire(),
                validation.getDateExpiration()
        );
    }
}

