package odk.malicompetences.backend.preuve;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.portfolio.PortfolioMediaStorageService;
import odk.malicompetences.backend.portfolio.TypeMedia;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PreuveService {

    private final PreuveRepository preuveRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final PreuveMapper preuveMapper;
    private final PortfolioMediaStorageService mediaStorage;

    public PreuveService(
            PreuveRepository preuveRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            PreuveMapper preuveMapper,
            PortfolioMediaStorageService mediaStorage
    ) {
        this.preuveRepository = preuveRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.preuveMapper = preuveMapper;
        this.mediaStorage = mediaStorage;
    }

    public PreuveResponse creerPreuve(PreuveRequest request) {
        CitoyenCompetence citoyenCompetence = citoyenCompetenceRepository.findById(request.citoyenCompetenceId())
                .orElseThrow(() -> new RuntimeException("Compétence du citoyen non trouvée avec l'id : " + request.citoyenCompetenceId()));

        verifierProprietaire(citoyenCompetence.getCitoyen().getId(), "CITOYEN");
        Preuve preuve = preuveMapper.toEntity(request, citoyenCompetence);
        Preuve saved = preuveRepository.save(preuve);
        return preuveMapper.toResponse(saved);
    }

    public CitoyenPreuveResponse creerPreuveFichier(
            UUID citoyenCompetenceId,
            TypePreuve type,
            MultipartFile fichier
    ) {
        CitoyenCompetence competence = citoyenCompetenceRepository.findById(citoyenCompetenceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compétence du citoyen introuvable"
                ));
        verifierProprietaire(competence.getCitoyen().getId(), "CITOYEN");
        if (type == null || !List.of(
                TypePreuve.DIPLOME,
                TypePreuve.CERTIFICAT,
                TypePreuve.ATTESTATION_EMPLOYEUR,
                TypePreuve.AUTRE
        ).contains(type)) {
            throw new IllegalArgumentException("Ce type de preuve ne peut pas être ajouté par le citoyen");
        }
        TypeMedia mediaType = typeMedia(fichier);
        String originalName = fichier == null ? null : fichier.getOriginalFilename();
        String safeName = originalName == null ? "" : originalName.replace('\\', '/');
        safeName = safeName.substring(safeName.lastIndexOf('/') + 1).trim();
        if (safeName.isBlank()) {
            throw new IllegalArgumentException("Le nom du fichier est obligatoire");
        }
        String titre = safeName.length() > 200 ? safeName.substring(0, 200) : safeName;
        String url = mediaStorage.store(fichier, mediaType);

        Preuve preuve = new Preuve();
        preuve.setCitoyenCompetence(competence);
        preuve.setType(type);
        preuve.setTitre(titre);
        preuve.setFichierUrl(url);
        preuve.setDateDepot(java.time.LocalDateTime.now());
        return CitoyenPreuveResponse.from(preuveRepository.save(preuve));
    }

    private TypeMedia typeMedia(MultipartFile fichier) {
        String originalName = fichier == null ? null : fichier.getOriginalFilename();
        String lowerName = originalName == null ? "" : originalName.toLowerCase(java.util.Locale.ROOT);
        if (lowerName.matches(".*\\.(jpg|jpeg|png|webp)$")) return TypeMedia.IMAGE;
        if (lowerName.matches(".*\\.(mp4|mov|webm)$")) return TypeMedia.VIDEO;
        if (lowerName.matches(".*\\.(pdf|doc|docx)$")) return TypeMedia.DOCUMENT;
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Format de fichier non pris en charge."
        );
    }

    @Transactional(readOnly = true)
    public PreuveResponse trouverParId(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preuve non trouvée avec l'id : " + id));
        return preuveMapper.toResponse(preuve);
    }

    @Transactional(readOnly = true)
    public List<PreuveResponse> listerParCitoyenCompetence(UUID citoyenCompetenceId) {
        CitoyenCompetence competence = citoyenCompetenceRepository.findById(citoyenCompetenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compétence du citoyen introuvable"));
        verifierProprietaire(competence.getCitoyen().getId(), "CITOYEN");
        return preuveRepository.findByCitoyenCompetenceId(citoyenCompetenceId).stream()
                .map(preuveMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CitoyenPreuveResponse> listerPourCompetenceCitoyen(
            UUID citoyenCompetenceId
    ) {
        CitoyenCompetence competence = citoyenCompetenceRepository.findById(citoyenCompetenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compétence du citoyen introuvable"));
        verifierProprietaire(competence.getCitoyen().getId(), "CITOYEN");
        return preuveRepository.findByCitoyenCompetenceId(citoyenCompetenceId).stream()
                .map(CitoyenPreuveResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CitoyenPreuveResponse> listerMesPreuves(UUID citoyenId) {
        verifierProprietaire(citoyenId, "CITOYEN");
        return preuveRepository.findByCitoyenCompetenceCitoyenId(citoyenId).stream()
                .map(CitoyenPreuveResponse::from)
                .toList();
    }

    public PreuveResponse modifierPreuve(UUID id, PreuveRequest request) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preuve non trouvée avec l'id : " + id));
        verifierProprietaire(preuve.getCitoyenCompetence().getCitoyen().getId(), "CITOYEN");

        preuveMapper.updateEntity(request, preuve);
        Preuve updated = preuveRepository.save(preuve);
        return preuveMapper.toResponse(updated);
    }

    public void supprimerPreuve(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preuve non trouvée avec l'id : " + id));
        verifierProprietaire(preuve.getCitoyenCompetence().getCitoyen().getId(), "CITOYEN");
        preuveRepository.delete(preuve);
    }

    public PreuveResponse getById(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Preuve introuvable"));

        verifierProprietaire(preuve.getCitoyenCompetence().getCitoyen().getId(), "CITOYEN");
        return preuveMapper.toResponse(preuve);
    }

    @Transactional(readOnly = true)
    public CitoyenPreuveResponse trouverPourCitoyen(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preuve introuvable"));
        verifierProprietaire(preuve.getCitoyenCompetence().getCitoyen().getId(), "CITOYEN");
        return CitoyenPreuveResponse.from(preuve);
    }

    public PreuveResponse modifier(UUID id, PreuveRequest request) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preuve introuvable"));
        verifierProprietaire(preuve.getCitoyenCompetence().getCitoyen().getId(), "CITOYEN");

        preuve.setType(request.type());
        preuve.setTitre(request.titre());
        preuve.setDescription(request.description());
        preuve.setFichierUrl(request.fichierUrl());

        return preuveMapper.toResponse(
                preuveRepository.save(preuve)
        );
    }

    public void supprimer(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preuve introuvable"));
        verifierProprietaire(preuve.getCitoyenCompetence().getCitoyen().getId(), "CITOYEN");

        preuveRepository.delete(preuve);
    }

    /** Vérifie directement que l'utilisateur connecté possède la ressource. */
    private void verifierProprietaire(UUID proprietaireId, String roleAttendu) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean administrateur = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (administrateur) return;
        boolean bonRole = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleAttendu));
        boolean bonUtilisateur = false;
        if (authentication != null && authentication.getName() != null && proprietaireId != null) {
            try { bonUtilisateur = proprietaireId.equals(UUID.fromString(authentication.getName())); }
            catch (IllegalArgumentException ignored) { bonUtilisateur = false; }
        }
        if (!bonRole || !bonUtilisateur) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé : cette ressource ne vous appartient pas.");
        }
    }

    private boolean estAdministrateur() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}