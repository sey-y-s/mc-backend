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
    private final odk.malicompetences.backend.evaluateur.EvaluateurCentreRepository evaluateurs;
        private final odk.malicompetences.backend.validation.ValidationRepository validations;
    private final odk.malicompetences.backend.miseenrelation.DemandeMiseEnRelationRepository relations;


    public PortfolioMediaAccessService(
            PreuveRepository preuveRepository,
            MediaRealisationRepository mediaRepository,
            CitoyenRepository citoyenRepository,
            odk.malicompetences.backend.evaluateur.EvaluateurCentreRepository evaluateurs,
                        odk.malicompetences.backend.validation.ValidationRepository validations,
            odk.malicompetences.backend.miseenrelation.DemandeMiseEnRelationRepository relations
    ) {
        this.preuveRepository = preuveRepository;
        this.mediaRepository = mediaRepository;
        this.citoyenRepository = citoyenRepository;
        this.evaluateurs = evaluateurs;
                this.validations = validations;
        this.relations = relations;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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
                if (!proprietairePreuve && !proprietairePortfolio && !photoDeProfil && !evaluateurPeutVoir(url, citoyenId)
                && !contactPeutVoir(url, citoyenId)) {
            throw new AccessDeniedException("Accès à ce média refusé");
        }
    }

    /** Un évaluateur voit les médias du portfolio d'un citoyen dont il évalue la demande (en attente). */
    private boolean evaluateurPeutVoir(String url, UUID evaluateurId) {
        return evaluateurs.findByCitoyenIdAndActifTrue(evaluateurId).stream()
                .flatMap(e -> validations.findByCentreIdAndStatut(
                        e.getCentre().getId(), odk.malicompetences.backend.validation.StatutValidation.EN_ATTENTE).stream())
                .filter(v -> v.getType() == odk.malicompetences.backend.validation.TypeValidation.PORTFOLIO_MEDIA)
                .anyMatch(v -> mediaRepository.existsByUrlAndCitoyenId(url, v.getCitoyenCompetence().getCitoyen().getId()));
    }

    /** Après une mise en relation acceptée, chacun voit la photo et les réalisations de l'autre. */
    private boolean contactPeutVoir(String url, UUID moi) {
        return relations.contactsEtablis(moi).stream()
                .anyMatch(id -> citoyenRepository.existsByIdAndPhotoUrl(id, url) || mediaRepository.existsByUrlAndCitoyenId(url, id));
    }
}
