package odk.malicompetences.backend.portfolio;

import odk.malicompetences.backend.portfolio.TypeMedia;

public record MediaRealisationRequest(
        TypeMedia type,
        String url,
        String nomFichier
) {
}