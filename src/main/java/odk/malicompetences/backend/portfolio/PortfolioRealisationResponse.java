package odk.malicompetences.backend.portfolio;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PortfolioRealisationResponse(
        UUID id,
        UUID citoyenId,
        String citoyenNomComplet,
        String titre,
        String description,
        LocalDate dateRealisation,
        String localisation,
        List<MediaRealisationResponse> medias
) {}