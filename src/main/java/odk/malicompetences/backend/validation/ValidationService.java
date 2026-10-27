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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public ValidationResponse creerValidation(ValidationRequest request, String authenticatedUserId) {
        UUID validateurId;
        try {
            validateurId = UUID.fromString(authenticatedUserId);
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException("Utilisateur authentifié invalide");
        }

        CitoyenCompetence citoyenCompetence = citoyenCompetenceRepository.findById(request.citoyenCompetenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Compétence du citoyen introuvable"));

        Preuve preuve = null;
        if (request.preuveId() != null) {
            preuve = preuveRepository.findById(request.preuveId())
                    .orElseThrow(() -> new ResourceNotFoundException("Preuve introuvable"));
            if (!preuve.getCitoyenCompetence().getId().equals(citoyenCompetence.getId())) {
                throw new IllegalArgumentException("La preuve ne correspond pas à la compétence du citoyen");
            }
        }

        Utilisateur valideePar = utilisateurRepository.findById(validateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur validateur introuvable"));

        Centre centre = null;
        if (request.centreId() != null) {
            centre = centreRepository.findById(request.centreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Centre introuvable"));
        }

        Validation validation = validationMapper.toEntity(request, citoyenCompetence, preuve, valideePar, centre);
        Validation saved = validationRepository.save(validation);
        return validationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ValidationResponse trouverParId(UUID id) {
        Validation validation = validationRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Validation non trouvée avec l'id : " + id));
        return validationMapper.toResponse(validation);
    }

    @Transactional(readOnly = true)
    public long compterToutes() {
        return validationRepository.count();
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

    @Transactional(readOnly = true)
    public Page<CitoyenValidationResponse> listerPourCitoyen(
            UUID citoyenId,
            StatutValidation statut,
            int page,
            int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("La pagination doit utiliser page >= 0 et size entre 1 et 100");
        }
        PageRequest pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "dateDemande")
        );
        Page<Validation> validations = statut == null
                ? validationRepository.findByCitoyenCompetenceCitoyenId(citoyenId, pageable)
                : validationRepository.findByCitoyenCompetenceCitoyenIdAndStatut(
                        citoyenId, statut, pageable
                );
        return validations.map(CitoyenValidationResponse::from);
    }

    @Transactional(readOnly = true)
    public List<CitoyenValidationResponse> listerPourCompetenceCitoyen(
            UUID citoyenCompetenceId,
            UUID citoyenId
    ) {
        return validationRepository
                .findByCitoyenCompetenceIdAndCitoyenCompetenceCitoyenId(
                        citoyenCompetenceId, citoyenId
                ).stream()
                .map(CitoyenValidationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CitoyenValidationResponse trouverPourCitoyen(UUID id, UUID citoyenId) {
        return validationRepository.findByIdAndCitoyenCompetenceCitoyenId(id, citoyenId)
                .map(CitoyenValidationResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Validation introuvable"));
    }

    // Aucune opération de modification ni de suppression d'une validation n'est exposée :
    // l'historique des décisions est immuable (consignes V1.2). Une correction = une nouvelle décision.

    public ValidationResponse getById(UUID id) {
        Validation validation = validationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Validation introuvable"));

        return validationMapper.toResponse(validation);
    }

    public ValidationResponse modifierStatut(
            UUID id,
            StatutValidation statut,
            String commentaire) {
        return decider(id, statut, commentaire, null, null);
    }

    /**
     * Enregistre LA décision d'une demande en attente. La décision et la mise à jour de la compétence
     * du citoyen (état, niveau validé, date, échéance) sont faites dans la même transaction.
     */
    public ValidationResponse decider(
            UUID id,
            StatutValidation statut,
            String commentaire,
            odk.malicompetences.backend.competence.NiveauCompetence niveauValide,
            UUID decideurId) {

        Validation validation = validationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Validation introuvable"));

        if (statut == null) {
            throw new IllegalArgumentException("Le statut est obligatoire");
        }
        if (validation.getStatut() != StatutValidation.EN_ATTENTE) {
            throw new IllegalStateException("Cette validation a déjà reçu une décision : elle ne peut plus être modifiée. "
                    + "Le citoyen peut la contester ; une contestation acceptée crée une nouvelle décision.");
        }
        if (statut == StatutValidation.EN_ATTENTE) {
            throw new IllegalArgumentException("Le nouveau statut doit être APPROUVEE ou REJETEE");
        }
        if (statut == StatutValidation.REJETEE && (commentaire == null || commentaire.isBlank())) {
            throw new IllegalArgumentException("Un commentaire est obligatoire pour rejeter une validation.");
        }

        validation.setStatut(statut);
        validation.setCommentaire(commentaire);
        validation.setDateValidation(LocalDateTime.now());
        if (statut == StatutValidation.APPROUVEE) {
            appliquerDecisionFavorable(validation, niveauValide);
        }
        return validationMapper.toResponse(validationRepository.save(validation));
    }

    /**
     * Décision corrective (contestation acceptée) : une NOUVELLE validation est créée, l'ancienne reste intacte.
     */
    public Validation decisionCorrective(Validation ancienne, StatutValidation statut, String motif,
                                         odk.malicompetences.backend.competence.NiveauCompetence niveauValide,
                                         Utilisateur decideur) {
        if (ancienne.getStatut() == StatutValidation.EN_ATTENTE) {
            throw new IllegalStateException("Cette demande n'a pas encore de décision à corriger.");
        }
        if (statut == null || statut == StatutValidation.EN_ATTENTE) {
            throw new IllegalArgumentException("La nouvelle décision doit être APPROUVEE ou REJETEE.");
        }
        Validation nouvelle = new Validation();
        nouvelle.setCitoyenCompetence(ancienne.getCitoyenCompetence());
        nouvelle.setPreuve(ancienne.getPreuve());
        nouvelle.setCentre(ancienne.getCentre());
        nouvelle.setType(ancienne.getType());
        nouvelle.setValideePar(decideur);
        nouvelle.setStatut(statut);
        nouvelle.setCommentaire(motif);
        nouvelle.setDateDemande(LocalDateTime.now());
        nouvelle.setDateValidation(LocalDateTime.now());
        nouvelle.setDateExpiration(ancienne.getDateExpiration());
        nouvelle.setDecisionCorrigee(ancienne);
        CitoyenCompetence cc = ancienne.getCitoyenCompetence();
        if (statut == StatutValidation.APPROUVEE) {
            appliquerDecisionFavorable(nouvelle, niveauValide);
        } else if (cc.getEtat() == odk.malicompetences.backend.competence.EtatCompetence.VALIDEE
                && ancienne.getStatut() == StatutValidation.APPROUVEE) {
            // La décision favorable corrigée ne fonde plus la validation de la compétence.
            cc.setEtat(cc.getPreuves() == null || cc.getPreuves().isEmpty()
                    ? odk.malicompetences.backend.competence.EtatCompetence.DECLAREE
                    : odk.malicompetences.backend.competence.EtatCompetence.ATTESTEE);
            cc.setNiveauValide(null);
            cc.setValideeLe(null);
            cc.setValideJusquau(null);
            citoyenCompetenceRepository.save(cc);
        }
        return validationRepository.save(nouvelle);
    }

    /** Compétence VALIDEE = niveau validé + date de validation + décision favorable cohérente. */
    private void appliquerDecisionFavorable(Validation validation,
                                            odk.malicompetences.backend.competence.NiveauCompetence niveauValide) {
        CitoyenCompetence cc = validation.getCitoyenCompetence();
        var niveau = niveauValide != null ? niveauValide : cc.getNiveau();
        validation.setNiveauValide(niveau);
        cc.setEtat(odk.malicompetences.backend.competence.EtatCompetence.VALIDEE);
        cc.setNiveauValide(niveau);
        cc.setValideeLe(validation.getDateValidation() != null ? validation.getDateValidation() : LocalDateTime.now());
        cc.setValideJusquau(validation.getDateExpiration());
        citoyenCompetenceRepository.save(cc);
    }

}
