package odk.malicompetences.backend.portfolio;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MediaRealisationService {

    private final MediaRealisationRepository repository;
    private final PortfolioRealisationRepository realisationRepository;
    private final MediaRealisationMapper mapper;

    public MediaRealisationService(
            MediaRealisationRepository repository,
            PortfolioRealisationRepository realisationRepository,
            MediaRealisationMapper mapper
    ) {
        this.repository = repository;
        this.realisationRepository = realisationRepository;
        this.mapper = mapper;
    }

    public MediaRealisationResponse ajouter(
            UUID realisationId,
            MediaRealisationRequest request
    ) {
        PortfolioRealisation realisation =
                realisationRepository.findById(realisationId)
                        .orElseThrow(() -> new IllegalArgumentException("Réalisation introuvable"));
        verifierProprietaire(realisation.getCitoyen().getId(), "CITOYEN");

        MediaRealisation media = new MediaRealisation();

        media.setPortfolioRealisation(realisation);
        verifierProprietaire(media.getPortfolioRealisation().getCitoyen().getId(), "CITOYEN");
        media.setType(request.type());
        media.setUrl(request.urlMedia());
        media.setNomFichier(request.legende());

        return mapper.toResponse(repository.save(media), realisationId);
    }

    @Transactional(readOnly = true)
    public List<MediaRealisationResponse> trouverParRealisation(
            UUID realisationId
    ) {
        return repository
                .findByPortfolioRealisationId(realisationId)
                .stream()
                .map((MediaRealisation media) -> mapper.toResponse(media, media.getPortfolioRealisation().getId()))
                .toList();
    }

    public MediaRealisationResponse getById(UUID id) {
        MediaRealisation media = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Média introuvable"));

        return mapper.toResponse(media, media.getPortfolioRealisation().getId());
    }

    public MediaRealisationResponse modifier(
            UUID id,
            MediaRealisationRequest request) {

        MediaRealisation media = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Média introuvable"));

        verifierProprietaire(media.getPortfolioRealisation().getCitoyen().getId(), "CITOYEN");
        media.setType(request.type());
        media.setUrl(request.urlMedia());
        media.setNomFichier(request.legende());

        return mapper.toResponse(
                repository.save(media), media.getPortfolioRealisation().getId()
        );
    }

    public void supprimer(UUID id) {
        MediaRealisation media = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Média introuvable"));

        verifierProprietaire(media.getPortfolioRealisation().getCitoyen().getId(), "CITOYEN");
        repository.delete(media);
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