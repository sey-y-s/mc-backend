package odk.malicompetences.backend.portfolio;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PortfolioRealisationService {

    private final PortfolioRealisationRepository portfolioRepository;
    private final CitoyenRepository citoyenRepository;
    private final PortfolioRealisationMapper portfolioMapper;

    public PortfolioRealisationService(
            PortfolioRealisationRepository portfolioRepository,
            CitoyenRepository citoyenRepository,
            PortfolioRealisationMapper portfolioMapper
    ) {
        this.portfolioRepository = portfolioRepository;
        this.citoyenRepository = citoyenRepository;
        this.portfolioMapper = portfolioMapper;
    }

    public PortfolioRealisationResponse creerPortfolio(PortfolioRealisationRequest request) {
        verifierProprietaire(request.citoyenId(), "CITOYEN");
        Citoyen citoyen = citoyenRepository.findById(request.citoyenId())
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Citoyen non trouvé"));

        PortfolioRealisation portfolio = portfolioMapper.toEntity(request, citoyen);
        return portfolioMapper.toResponse(portfolioRepository.save(portfolio));
    }

    @Transactional(readOnly = true)
    public PortfolioRealisationResponse trouverParId(UUID id) {
        PortfolioRealisation portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Réalisation du portfolio non trouvée"));
        verifierProprietaire(portfolio.getCitoyen().getId(), "CITOYEN");
        return portfolioMapper.toResponse(portfolio);
    }

    @Transactional(readOnly = true)
    public List<PortfolioRealisationResponse> listerParCitoyen(UUID citoyenId) {
        verifierProprietaire(citoyenId, "CITOYEN");
        return portfolioRepository.findByCitoyenId(citoyenId).stream()
                .map(portfolioMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PortfolioRealisationResponse modifierPortfolio(UUID id, PortfolioRealisationRequest request) {
        PortfolioRealisation portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Réalisation du portfolio non trouvée"));
        verifierProprietaire(portfolio.getCitoyen().getId(), "CITOYEN");

        portfolioMapper.updateEntity(request, portfolio);
        return portfolioMapper.toResponse(portfolioRepository.save(portfolio));
    }

    public void supprimerPortfolio(UUID id) {
        PortfolioRealisation portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Réalisation du portfolio non trouvée"));
        verifierProprietaire(portfolio.getCitoyen().getId(), "CITOYEN");
        portfolioRepository.delete(portfolio);
    }

    public PortfolioRealisationResponse getById(UUID id) {
        PortfolioRealisation realisation =
                portfolioRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Réalisation introuvable"));

        verifierProprietaire(realisation.getCitoyen().getId(), "CITOYEN");
        return portfolioMapper.toResponse(realisation);
    }

    public PortfolioRealisationResponse modifier(
            UUID id,
            PortfolioRealisationRequest request) {

        PortfolioRealisation realisation =
                portfolioRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Réalisation introuvable"));
        verifierProprietaire(realisation.getCitoyen().getId(), "CITOYEN");

        realisation.setTitre(request.titre());
        realisation.setDescription(request.description());
        realisation.setDateRealisation(request.dateRealisation());
        realisation.setLienUrl(request.lienUrl());

        return portfolioMapper.toResponse(
                portfolioRepository.save(realisation)
        );
    }

    public void supprimer(UUID id) {
        PortfolioRealisation realisation =
                 portfolioRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Réalisation introuvable"));
        verifierProprietaire(realisation.getCitoyen().getId(), "CITOYEN");

        portfolioRepository.delete(realisation);
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