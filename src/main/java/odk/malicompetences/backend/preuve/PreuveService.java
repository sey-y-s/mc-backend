package odk.malicompetences.backend.preuve;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.statut.StatutPreuve;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.util.PrivateFileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PreuveService {

    private final PreuveRepository preuveRepository;
    private final CitoyenCompetenceRepository competenceRepository;
    private final CitoyenRepository citoyenRepository;
    private final PreuveMapper preuveMapper;
    private final PrivateFileStorageService storageService;

    public PreuveService(
            PreuveRepository preuveRepository,
            CitoyenCompetenceRepository competenceRepository,
            CitoyenRepository citoyenRepository,
            PreuveMapper preuveMapper,
            PrivateFileStorageService storageService
    ) {
        this.preuveRepository = preuveRepository;
        this.competenceRepository = competenceRepository;
        this.citoyenRepository = citoyenRepository;
        this.preuveMapper = preuveMapper;
        this.storageService = storageService;
    }

    // -------------------------------------------------------------------------
    // Création (métadonnées seules — fichier optionnel séparément)
    // -------------------------------------------------------------------------

    public PreuveResponse creer(UUID citoyenCompetenceId, PreuveRequest request) {
        CitoyenCompetence competence = chargerCompetenceEtVerifierProprietaire(citoyenCompetenceId);
        Preuve preuve = preuveMapper.toEntity(request, competence);
        return preuveMapper.toResponse(preuveRepository.save(preuve));
    }

    // -------------------------------------------------------------------------
    // Création avec fichier (multipart)
    // -------------------------------------------------------------------------

    public PreuveResponse creerAvecFichier(UUID citoyenCompetenceId,
                                           PreuveRequest request,
                                           MultipartFile fichier) {
        CitoyenCompetence competence = chargerCompetenceEtVerifierProprietaire(citoyenCompetenceId);
        String cle = storageService.store(fichier);
        Preuve preuve = preuveMapper.toEntity(request, competence);
        preuve.setCleStockagePrive(cle);
        return preuveMapper.toResponse(preuveRepository.save(preuve));
    }

    // -------------------------------------------------------------------------
    // Lecture
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PreuveResponse> listerParCompetence(UUID citoyenCompetenceId) {
        chargerCompetenceEtVerifierProprietaire(citoyenCompetenceId);
        return preuveRepository.findByCitoyenCompetenceIdAndStatutNot(citoyenCompetenceId, StatutPreuve.ARCHIVEE)
                .stream().map(preuveMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PreuveResponse> listerMesPreuves(UUID utilisateurId) {
        Citoyen citoyen = citoyenRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));
        return preuveRepository.findByCitoyenCompetenceCitoyenIdAndStatutNot(citoyen.getId(), StatutPreuve.ARCHIVEE)
                .stream().map(preuveMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PreuveResponse trouver(UUID id) {
        Preuve preuve = chargerEtVerifier(id);
        return preuveMapper.toResponse(preuve);
    }

    // -------------------------------------------------------------------------
    // Mise à jour partielle (titre, description, visibleProfil)
    // -------------------------------------------------------------------------

    public PreuveResponse patcher(UUID id, PreuveRequest request) {
        Preuve preuve = chargerEtVerifier(id);
        preuveMapper.patch(request, preuve);
        return preuveMapper.toResponse(preuveRepository.save(preuve));
    }

    // -------------------------------------------------------------------------
    // Archivage (soft delete)
    // -------------------------------------------------------------------------

    public void archiver(UUID id) {
        Preuve preuve = chargerEtVerifier(id);
        preuve.setStatut(StatutPreuve.ARCHIVEE);
        preuveRepository.save(preuve);
    }

    // -------------------------------------------------------------------------
    // Téléchargement du fichier (propriétaire ou ADMIN)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Resource chargerFichier(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preuve introuvable"));
        verifierAccesFichier(preuve);
        if (preuve.getCleStockagePrive() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Aucun fichier associé à cette preuve.");
        }
        return storageService.loadAsResource(preuve.getCleStockagePrive());
    }

    @Transactional(readOnly = true)
    public String cleStockage(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preuve introuvable"));
        verifierAccesFichier(preuve);
        return preuve.getCleStockagePrive();
    }

    public org.springframework.http.MediaType contentType(String cle) {
        return storageService.contentType(cle);
    }

    // -------------------------------------------------------------------------
    // Helpers privés
    // -------------------------------------------------------------------------

    private CitoyenCompetence chargerCompetenceEtVerifierProprietaire(UUID citoyenCompetenceId) {
        CitoyenCompetence competence = competenceRepository.findById(citoyenCompetenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compétence du citoyen introuvable"));
        if (!estAdmin()) {
            UUID utilisateurId = utilisateurConnecteId();
            Citoyen citoyen = citoyenRepository.findByUtilisateurId(utilisateurId)
                    .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));
            if (!competence.getCitoyen().getId().equals(citoyen.getId())) {
                throw new AccessDeniedException("Cette compétence ne vous appartient pas.");
            }
        }
        return competence;
    }

    private Preuve chargerEtVerifier(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preuve introuvable"));
        if (!estAdmin()) {
            UUID utilisateurId = utilisateurConnecteId();
            Citoyen citoyen = citoyenRepository.findByUtilisateurId(utilisateurId)
                    .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));
            if (!preuve.getCitoyenCompetence().getCitoyen().getId().equals(citoyen.getId())) {
                throw new AccessDeniedException("Cette preuve ne vous appartient pas.");
            }
        }
        return preuve;
    }

    private void verifierAccesFichier(Preuve preuve) {
        if (estAdmin()) return;
        UUID utilisateurId = utilisateurConnecteId();
        Citoyen citoyen = citoyenRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));
        if (!preuve.getCitoyenCompetence().getCitoyen().getId().equals(citoyen.getId())) {
            throw new AccessDeniedException("Accès refusé.");
        }
    }

    private boolean estAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    private UUID utilisateurConnecteId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Non authentifié.");
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("Token invalide.");
        }
    }
}
