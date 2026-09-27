package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.preuve.Preuve;
import odk.malicompetences.backend.preuve.PreuveRepository;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ValidationService {

    private final ValidationRepository validationRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final PreuveRepository preuveRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CentreRepository centreRepository;
    private final ValidationMapper mapper;

    public ValidationService(
            ValidationRepository validationRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            PreuveRepository preuveRepository,
            UtilisateurRepository utilisateurRepository,
            CentreRepository centreRepository,
            ValidationMapper mapper
    ) {
        this.validationRepository = validationRepository;
        this.citoyenCompetenceRepository =
                citoyenCompetenceRepository;
        this.preuveRepository = preuveRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.centreRepository = centreRepository;
        this.mapper = mapper;
    }

    public ValidationResponse creer(
            UUID citoyenCompetenceId,
            ValidationRequest request
    ) {
        CitoyenCompetence competence =
                citoyenCompetenceRepository.findById(
                        citoyenCompetenceId
                ).orElseThrow(() -> new IllegalArgumentException(
                        "Compétence du citoyen introuvable"
                ));

        Utilisateur validateur =
                utilisateurRepository.findById(
                        request.valideeParId()
                ).orElseThrow(() -> new IllegalArgumentException(
                        "Validateur introuvable"
                ));

        Validation validation = new Validation();

        validation.setCitoyenCompetence(competence);
        validation.setValideePar(validateur);
        validation.setType(request.type());
        validation.setStatut(StatutValidation.EN_ATTENTE);
        validation.setCommentaire(request.commentaire());

        if (request.preuveId() != null) {
            Preuve preuve = preuveRepository.findById(
                    request.preuveId()
            ).orElseThrow(() -> new IllegalArgumentException(
                    "Preuve introuvable"
            ));

            validation.setPreuve(preuve);
        }

        if (request.centreId() != null) {
            Centre centre = centreRepository.findById(
                    request.centreId()
            ).orElseThrow(() -> new IllegalArgumentException(
                    "Centre introuvable"
            ));

            validation.setCentre(centre);
        }

        return mapper.toResponse(
                validationRepository.save(validation)
        );
    }

    @Transactional(readOnly = true)
    public List<ValidationResponse> trouverParCompetence(
            UUID citoyenCompetenceId
    ) {
        return validationRepository
                .findByCitoyenCompetenceId(citoyenCompetenceId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}