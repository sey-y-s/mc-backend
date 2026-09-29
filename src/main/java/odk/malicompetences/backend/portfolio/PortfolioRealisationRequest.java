package odk.malicompetences.backend.portfolio;

import java.time.LocalDate;

public record PortfolioRealisationRequest(
        String titre,
        String description,
        LocalDate dateRealisation,
        String lienUrl
) {
}