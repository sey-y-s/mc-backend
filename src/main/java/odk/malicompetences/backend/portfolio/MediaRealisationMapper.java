package odk.malicompetences.backend.portfolio;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MediaRealisationMapper {

    public MediaRealisationResponse toResponse(
            MediaRealisation media, UUID portfolioId
    ) {
        return new MediaRealisationResponse(
                media.getId(),
                media.getType(),
                media.getUrl(),
                media.getNomFichier(),
                portfolioId
        );
    }
}