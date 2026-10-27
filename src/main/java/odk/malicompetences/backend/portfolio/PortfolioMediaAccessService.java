package odk.malicompetences.backend.portfolio;

import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.preuve.PreuveRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PortfolioMediaAccessService {

    private final PreuveRepository preuveRepository;
    private final MediaRealisationRepository mediaRepository;
    private final CitoyenRepository citoyenRepository;

    public PortfolioMediaAccessService(
            PreuveRepository preuveRepository,
            MediaRealisationRepository mediaRepository,
            CitoyenRepository citoyenRepository
    ) {
        this.preuveRepository = preuveRepository;
        this.mediaRepository = mediaRepository;
        this.citoyenRepository = citoyenRepository;
    }

    public void verifierAcces(String fileName, Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return;
        }
        UUID citoyenId;
        try {
            citoyenId = UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException("Accès à ce média refusé");
        }
        String url = PortfolioMediaStorageService.MEDIA_URL_PREFIX + fileName;
        boolean proprietairePreuve = preuveRepository.existsByUrlAndCitoyenId(url, citoyenId);
        boolean proprietairePortfolio = mediaRepository.existsByUrlAndCitoyenId(url, citoyenId);
        // La photo de profil est stockée au même endroit : son propriétaire doit pouvoir l'afficher.
        boolean photoDeProfil = citoyenRepository.existsByIdAndPhotoUrl(citoyenId, url);
        if (!proprietairePreuve && !proprietairePortfolio && !photoDeProfil) {
            throw new AccessDeniedException("Accès à ce média refusé");
        }
    }
}
