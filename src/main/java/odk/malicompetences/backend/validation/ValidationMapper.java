package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.preuve.Preuve;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
        validation.setStatut(request.statut());
        validation.setCommentaire(request.commentaire());
        validation.setDateExpiration(request.dateExpiration());

        // Si le statut est APPROUVEE ou REJETEE à la création, on fixe la date de validation
        if (request.statut() == StatutValidation.APPROUVEE || request.statut() == StatutValidation.REJETEE) {
            validation.setDateValidation(LocalDateTime.now());
        }

        return validation;
    }

    public void updateEntity(ValidationRequest request, Validation validation, CitoyenCompetence citoyenCompetence, Preuve preuve, Utilisateur valideePar, Centre centre) {
        if (request != null && validation != null) {
            if (citoyenCompetence != null) {
                validation.setCitoyenCompetence(citoyenCompetence);
            }
            validation.setPreuve(preuve); // Peut être null
            if (valideePar != null) {
                validation.setValideePar(valideePar);
            }
            validation.setCentre(centre); // Peut être null
            validation.setType(request.type());
            validation.setCommentaire(request.commentaire());
            validation.setDateExpiration(request.dateExpiration());

            // Gestion du changement de statut
            if (request.statut() != validation.getStatut()) {
                if ((request.statut() == StatutValidation.APPROUVEE || request.statut() == StatutValidation.REJETEE)
                        && validation.getDateValidation() == null) {
                    validation.setDateValidation(LocalDateTime.now());
                }
                validation.setStatut(request.statut());
            }
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