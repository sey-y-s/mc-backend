package odk.malicompetences.backend.portfolio;

import java.util.UUID;

public record MediaRealisationResponse(
        UUID id,
        TypeMedia type,
        String urlMedia,
        String legende,
        UUID portfolioId
) {}