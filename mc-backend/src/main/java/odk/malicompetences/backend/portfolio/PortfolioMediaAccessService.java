package odk.malicompetences.backend.portfolio;

import odk.malicompetences.backend.preuve.PreuveRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PortfolioMediaAccessService {

    private final PreuveRepository preuveRepository;
    private final MediaRealisationRepository mediaRepository;

    public PortfolioMediaAccessService(
            PreuveRepository preuveRepository,
            MediaRealisationRepository mediaRepository
    ) {
        this.preuveRepository = preuveRepository;
        this.mediaRepository = mediaRepository;
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
        if (!proprietairePreuve && !proprietairePortfolio) {
            throw new AccessDeniedException("Accès à ce média refusé");
        }
    }
}
