package odk.malicompetences.backend.portfolio;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PortfolioRealisationService {

    private final PortfolioRealisationRepository repository;
    private final CitoyenRepository citoyenRepository;
    private final PortfolioRealisationMapper mapper;

    public PortfolioRealisationService(
            PortfolioRealisationRepository repository,
            CitoyenRepository citoyenRepository,
            PortfolioRealisationMapper mapper
    ) {
        this.repository = repository;
        this.citoyenRepository = citoyenRepository;
        this.mapper = mapper;
    }

    public PortfolioRealisationResponse ajouter(
            UUID citoyenId,
            PortfolioRealisationRequest request
    ) {
        Citoyen citoyen = citoyenRepository.findById(citoyenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Citoyen introuvable"
                ));

        PortfolioRealisation realisation =
                new PortfolioRealisation();

        realisation.setCitoyen(citoyen);
        realisation.setTitre(request.titre());
        realisation.setDescription(request.description());
        realisation.setDateRealisation(request.dateRealisation());
        realisation.setLienUrl(request.lienUrl());

        return mapper.toResponse(repository.save(realisation));
    }

    @Transactional(readOnly = true)
    public List<PortfolioRealisationResponse> trouverParCitoyen(
            UUID citoyenId
    ) {
        return repository.findByCitoyenId(citoyenId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}