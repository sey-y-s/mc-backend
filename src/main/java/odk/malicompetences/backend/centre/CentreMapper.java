package odk.malicompetences.backend.centre;

import odk.malicompetences.backend.territoire.Commune;
import org.springframework.stereotype.Component;

@Component
public class CentreMapper {
    public Centre toEntity(CentreRequest request, Commune commune) {
        Centre centre = new Centre();
        appliquer(request, centre, commune);
        return centre;
    }

    public void updateEntityFromRequest(CentreRequest request, Centre centre, Commune commune) {
        appliquer(request, centre, commune);
    }

    private void appliquer(CentreRequest request, Centre centre, Commune commune) {
        centre.setNom(request.nom());
        centre.setDescription(request.description());
        centre.setAdresse(request.adresse());
        centre.setTelephone(request.telephone());
        centre.setEmail(request.email());
        centre.setCommune(commune);
    }

    public CentreResponse toResponse(Centre centre) {
        Commune commune = centre.getCommune();
        return new CentreResponse(centre.getId(), centre.getNom(), centre.getDescription(),
                centre.getAdresse(), centre.getTelephone(), centre.getEmail(), commune.getId(), commune.getNom(),
                centre.getStatut(), centre.getCreeLe());
    }
}
