package odk.malicompetences.backend.portfolio;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
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
        Citoyen citoyen = citoyenRepository.findById(request.citoyenId())
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé"));

        PortfolioRealisation portfolio = portfolioMapper.toEntity(request, citoyen);
        return portfolioMapper.toResponse(portfolioRepository.save(portfolio));
    }

    @Transactional(readOnly = true)
    public PortfolioRealisationResponse trouverParId(UUID id) {
        PortfolioRealisation portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réalisation du portfolio non trouvée"));
        return portfolioMapper.toResponse(portfolio);
    }

    @Transactional(readOnly = true)
    public List<PortfolioRealisationResponse> listerParCitoyen(UUID citoyenId) {
        return portfolioRepository.findByCitoyenId(citoyenId).stream()
                .map(portfolioMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PortfolioRealisationResponse modifierPortfolio(UUID id, PortfolioRealisationRequest request) {
        PortfolioRealisation portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réalisation du portfolio non trouvée"));

        portfolioMapper.updateEntity(request, portfolio);
        return portfolioMapper.toResponse(portfolioRepository.save(portfolio));
    }

    public void supprimerPortfolio(UUID id) {
        if (!portfolioRepository.existsById(id)) {
            throw new RuntimeException("Réalisation du portfolio non trouvée");
        }
        portfolioRepository.deleteById(id);
    }
}