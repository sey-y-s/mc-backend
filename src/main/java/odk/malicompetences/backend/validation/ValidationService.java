package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.preuve.Preuve;
import odk.malicompetences.backend.preuve.PreuveRepository;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ValidationService {

    private final ValidationRepository validationRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final PreuveRepository preuveRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CentreRepository centreRepository;
    private final ValidationMapper validationMapper;

    public ValidationService(
            ValidationRepository validationRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            PreuveRepository preuveRepository,
            UtilisateurRepository utilisateurRepository,
            CentreRepository centreRepository,
            ValidationMapper validationMapper
    ) {
        this.validationRepository = validationRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.preuveRepository = preuveRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.centreRepository = centreRepository;
        this.validationMapper = validationMapper;
    }

    public ValidationResponse creerValidation(ValidationRequest request) {
        CitoyenCompetence citoyenCompetence = citoyenCompetenceRepository.findById(request.citoyenCompetenceId())
                .orElseThrow(() -> new RuntimeException("Compétence du citoyen non trouvée avec l'id : " + request.citoyenCompetenceId()));

        Preuve preuve = null;
        if (request.preuveId() != null) {
            preuve = preuveRepository.findById(request.preuveId())
                    .orElseThrow(() -> new RuntimeException("Preuve non trouvée avec l'id : " + request.preuveId()));
        }

        Utilisateur valideePar = utilisateurRepository.findById(request.valideeParId())
                .orElseThrow(() -> new RuntimeException("Utilisateur validateur non trouvé avec l'id : " + request.valideeParId()));

        Centre centre = null;
        if (request.centreId() != null) {
            centre = centreRepository.findById(request.centreId())
                    .orElseThrow(() -> new RuntimeException("Centre non trouvé avec l'id : " + request.centreId()));
        }

        Validation validation = validationMapper.toEntity(request, citoyenCompetence, preuve, valideePar, centre);
        Validation saved = validationRepository.save(validation);
        return validationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ValidationResponse trouverParId(UUID id) {
        Validation validation = validationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Validation non trouvée avec l'id : " + id));
        return validationMapper.toResponse(validation);
    }

    @Transactional(readOnly = true)
    public List<ValidationResponse> listerToutes() {
        return validationRepository.findAll().stream()
                .map(validationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ValidationResponse> listerParCitoyenCompetence(UUID citoyenCompetenceId) {
        return validationRepository.findAll().stream()
                .filter(v -> v.getCitoyenCompetence().getId().equals(citoyenCompetenceId))
                .map(validationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ValidationResponse modifierValidation(UUID id, ValidationRequest request) {
        Validation validation = validationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Validation non trouvée avec l'id : " + id));

        CitoyenCompetence citoyenCompetence = null;
        if (request.citoyenCompetenceId() != null) {
            citoyenCompetence = citoyenCompetenceRepository.findById(request.citoyenCompetenceId())
                    .orElseThrow(() -> new RuntimeException("Compétence du citoyen non trouvée avec l'id : " + request.citoyenCompetenceId()));
        }

        Preuve preuve = null;
        if (request.preuveId() != null) {
            preuve = preuveRepository.findById(request.preuveId())
                    .orElseThrow(() -> new RuntimeException("Preuve non trouvée avec l'id : " + request.preuveId()));
        }

        Utilisateur valideePar = null;
        if (request.valideeParId() != null) {
            valideePar = utilisateurRepository.findById(request.valideeParId())
                    .orElseThrow(() -> new RuntimeException("Utilisateur validateur non trouvé avec l'id : " + request.valideeParId()));
        }

        Centre centre = null;
        if (request.centreId() != null) {
            centre = centreRepository.findById(request.centreId())
                    .orElseThrow(() -> new RuntimeException("Centre non trouvé avec l'id : " + request.centreId()));
        }

        validationMapper.updateEntity(request, validation, citoyenCompetence, preuve, valideePar, centre);
        Validation updated = validationRepository.save(validation);
        return validationMapper.toResponse(updated);
    }

    public void supprimerValidation(UUID id) {
        if (!validationRepository.existsById(id)) {
            throw new RuntimeException("Validation non trouvée avec l'id : " + id);
        }
        validationRepository.deleteById(id);
    }

    public ValidationResponse getById(UUID id) {
        Validation validation = validationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Validation introuvable"));

        return mapper.toResponse(validation);
    }

    public ValidationResponse modifierStatut(
            UUID id,
            StatutValidation statut,
            String commentaire) {

        Validation validation = validationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Validation introuvable"));

        validation.setStatut(statut);
        validation.setCommentaire(commentaire);

        if (statut == StatutValidation.APPROUVEE
                || statut == StatutValidation.REJETEE) {
            validation.setDateValidation(LocalDateTime.now());
        }

        return mapper.toResponse(
                validationRepository.save(validation)
        );
    }

}