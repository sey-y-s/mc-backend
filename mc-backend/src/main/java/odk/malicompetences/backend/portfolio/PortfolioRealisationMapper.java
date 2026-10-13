package odk.malicompetences.backend.portfolio;

import odk.malicompetences.backend.citoyen.Citoyen;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PortfolioRealisationMapper {

    public PortfolioRealisation toEntity(PortfolioRealisationRequest request, Citoyen citoyen) {
        if (request == null) return null;

        PortfolioRealisation portfolio = new PortfolioRealisation();
        portfolio.setCitoyen(citoyen);
        portfolio.setTitre(request.titre());
        portfolio.setDescription(request.description());
        portfolio.setDateRealisation(request.dateRealisation());
        portfolio.setLienUrl(request.localisation());

        if (request.medias() != null) {
            for (MediaRealisationRequest mediaReq : request.medias()) {
                MediaRealisation media = new MediaRealisation();
                media.setType(mediaReq.type());
                media.setUrl(mediaReq.urlMedia());
                media.setNomFichier(mediaReq.legende());
                media.setPortfolioRealisation(portfolio);
                portfolio.getMedias().add(media);
            }
        }

        return portfolio;
    }

    public void updateEntity(PortfolioRealisationRequest request, PortfolioRealisation portfolio) {
        if (request != null && portfolio != null) {
            portfolio.setTitre(request.titre());
            portfolio.setDescription(request.description());
            portfolio.setDateRealisation(request.dateRealisation());
            portfolio.setLienUrl(request.localisation());

            if (request.medias() != null) {
                portfolio.getMedias().clear();
                for (MediaRealisationRequest mediaReq : request.medias()) {
                    MediaRealisation media = new MediaRealisation();
                    media.setType(mediaReq.type());
                    media.setUrl(mediaReq.urlMedia());
                    media.setNomFichier(mediaReq.legende());
                    media.setPortfolioRealisation(portfolio);
                    portfolio.getMedias().add(media);
                }
            }
        }
    }

    public PortfolioRealisationResponse toResponse(PortfolioRealisation portfolio) {
        if (portfolio == null) return null;

        UUID citoyenId = portfolio.getCitoyen() != null ? portfolio.getCitoyen().getId() : null;
        String citoyenNomComplet = portfolio.getCitoyen() != null ? portfolio.getCitoyen().getPrenom() + " " + portfolio.getCitoyen().getNom() : null;

        List<MediaRealisationResponse> mediaResponses = portfolio.getMedias().stream()
                .map(m -> new MediaRealisationResponse(
                        m.getId(),
                        m.getType(),
                        m.getUrl(),
                        m.getNomFichier(),
                        portfolio.getId()
                ))
                .collect(Collectors.toList());

        return new PortfolioRealisationResponse(
                portfolio.getId(),
                citoyenId,
                citoyenNomComplet,
                portfolio.getTitre(),
                portfolio.getDescription(),
                portfolio.getDateRealisation(),
                portfolio.getLienUrl(),
                mediaResponses
        );
    }
}