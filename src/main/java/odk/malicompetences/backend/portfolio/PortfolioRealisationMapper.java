package odk.malicompetences.backend.portfolio;

import org.springframework.stereotype.Component;

@Component
public class PortfolioRealisationMapper {

    public PortfolioRealisationResponse toResponse(
            PortfolioRealisation realisation
    ) {
        return new PortfolioRealisationResponse(
                realisation.getId(),
                realisation.getTitre(),
                realisation.getDescription(),
                realisation.getDateRealisation(),
                realisation.getLienUrl()
        );
    }
}