package odk.malicompetences.backend.portfolio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MediaRealisationService {

    private final MediaRealisationRepository repository;
    private final PortfolioRealisationRepository realisationRepository;
    private final MediaRealisationMapper mapper;

    public MediaRealisationService(
            MediaRealisationRepository repository,
            PortfolioRealisationRepository realisationRepository,
            MediaRealisationMapper mapper
    ) {
        this.repository = repository;
        this.realisationRepository = realisationRepository;
        this.mapper = mapper;
    }

    public MediaRealisationResponse ajouter(
            UUID realisationId,
            MediaRealisationRequest request
    ) {
        PortfolioRealisation realisation =
                realisationRepository.findById(realisationId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Réalisation introuvable"
                        ));

        MediaRealisation media = new MediaRealisation();

        media.setPortfolioRealisation(realisation);
        media.setType(request.type());
        media.setUrl(request.url());
        media.setNomFichier(request.nomFichier());

        return mapper.toResponse(repository.save(media));
    }

    @Transactional(readOnly = true)
    public List<MediaRealisationResponse> trouverParRealisation(
            UUID realisationId
    ) {
        return repository
                .findByPortfolioRealisationId(realisationId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}