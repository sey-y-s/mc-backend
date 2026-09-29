package odk.malicompetences.backend.portfolio;

import java.time.LocalDate;
import java.util.UUID;

public record PortfolioRealisationResponse(
        UUID id,
        String titre,
        String description,
        LocalDate dateRealisation,
        String lienUrl
) {
}