package odk.malicompetences.backend.competence;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.statut.SituationCompetence;
import odk.malicompetences.backend.common.statut.StatutValidationCompetence;
import odk.malicompetences.backend.validation.DemandeValidationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CitoyenCompetenceService {
    private final CitoyenCompetenceRepository repository;
    private final CitoyenRepository citoyenRepository;
    private final CompetenceRepository competenceRepository;
    private final CitoyenCompetenceMapper mapper;
    private final DemandeValidationRepository demandeValidationRepository;

    public CitoyenCompetenceService(
            CitoyenCompetenceRepository repository,
            CitoyenRepository citoyenRepository,
            CompetenceRepository competenceRepository,
            CitoyenCompetenceMapper mapper,
            DemandeValidationRepository demandeValidationRepository
    ) {
        this.repository = repository;
        this.citoyenRepository = citoyenRepository;
        this.competenceRepository = competenceRepository;
        this.mapper = mapper;
        this.demandeValidationRepository = demandeValidationRepository;
    }

    public CitoyenCompetenceResponse ajouter(UUID citoyenId, CitoyenCompetenceRequest request) {
        Citoyen citoyen = chargerCitoyen(citoyenId);
        verifierProprietaire(citoyen);
        if (request.competenceId() == null) throw new IllegalArgumentException("L'identifiant de la compétence est obligatoire.");
        Competence competence = competenceRepository.findById(request.competenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Compétence introuvable"));
        if (repository.existsByCitoyenIdAndCompetenceId(citoyenId, request.competenceId())) {
            throw new IllegalStateException("Cette compétence est déjà déclarée");
        }

        CitoyenCompetence cc = new CitoyenCompetence();
        cc.setCitoyen(citoyen);
        cc.setCompetence(competence);
        cc.setNiveauDeclare(request.niveauDeclare());
        cc.setStatutValidation(StatutValidationCompetence.NON_VERIFIEE);
        cc.setSituation(request.situation() == null ? SituationCompetence.PRATIQUEE : request.situation());
        cc.setDisponibilite(Boolean.TRUE.equals(request.disponibilite()));
        return mapper.toResponse(repository.save(cc));
    }

    @Transactional(readOnly = true)
    public List<CitoyenCompetenceResponse> trouverParCitoyen(UUID citoyenId) {
        Citoyen citoyen = chargerCitoyen(citoyenId);
        verifierLecture(citoyen);
        return repository.findByCitoyenId(citoyenId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CitoyenCompetenceResponse trouverParId(UUID citoyenId, UUID id) {
        Citoyen citoyen = chargerCitoyen(citoyenId);
        verifierLecture(citoyen);
        return mapper.toResponse(chargerCompetenceCitoyen(citoyenId, id));
    }

    public CitoyenCompetenceResponse modifier(UUID citoyenId, UUID id, CitoyenCompetenceRequest request) {
        Citoyen citoyen = chargerCitoyen(citoyenId);
        verifierProprietaire(citoyen);
        CitoyenCompetence cc = chargerCompetenceCitoyen(citoyenId, id);

        if (request.niveauDeclare() != null) cc.setNiveauDeclare(request.niveauDeclare());
        if (request.situation() != null) cc.setSituation(request.situation());
        if (request.disponibilite() != null) cc.setDisponibilite(request.disponibilite());
        // Le corps PATCH ne peut pas remplacer la compétence ni toucher à son statut de validation.
        return mapper.toResponse(repository.save(cc));
    }

    public void supprimer(UUID citoyenId, UUID id) {
        Citoyen citoyen = chargerCitoyen(citoyenId);
        verifierProprietaire(citoyen);
        CitoyenCompetence cc = chargerCompetenceCitoyen(citoyenId, id);

        if (cc.getStatutValidation() != StatutValidationCompetence.NON_VERIFIEE
                && cc.getStatutValidation() != StatutValidationCompetence.REJETEE) {
            throw new IllegalStateException("Une compétence en cours de validation ou déjà validée ne peut pas être supprimée.");
        }
        if (demandeValidationRepository.existsByCitoyenCompetence_Id(cc.getId())) {
            throw new IllegalStateException("Cette compétence possède un historique de demandes de validation et ne peut pas être supprimée.");
        }
        repository.delete(cc);
    }

    private Citoyen chargerCitoyen(UUID citoyenId) {
        return citoyenRepository.findById(citoyenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citoyen introuvable"));
    }

    private CitoyenCompetence chargerCompetenceCitoyen(UUID citoyenId, UUID id) {
        return repository.findById(id)
                .filter(cc -> cc.getCitoyen().getId().equals(citoyenId))
                .orElseThrow(() -> new ResourceNotFoundException("Compétence du citoyen introuvable"));
    }

    private void verifierLecture(Citoyen citoyen) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = auth != null && auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!admin && (auth == null || !citoyen.getUtilisateur().getId().toString().equals(auth.getName()))) {
            throw new AccessDeniedException("Accès refusé.");
        }
    }

    private void verifierProprietaire(Citoyen citoyen) {
        verifierLecture(citoyen);
    }
}
