package odk.malicompetences.backend.centre;

import org.springframework.stereotype.Component;

@Component
public class CentreMapper {

    // Convertit un DTO Request en Entité JPA
    public Centre toEntity(CentreRequest request) {
        if (request == null) {
            return null;
        }
        Centre centre = new Centre();
        centre.setNom(request.nom());
        centre.setDescription(request.description());
        centre.setAdresse(request.adresse());
        centre.setTelephone(request.telephone());
        return centre;
    }

    // Met à jour une entité existante à partir d'un DTO Request
    public void updateEntityFromRequest(CentreRequest request, Centre centre) {
        if (request != null && centre != null) {
            centre.setNom(request.nom());
            centre.setDescription(request.description());
            centre.setAdresse(request.adresse());
            centre.setTelephone(request.telephone());
        }
    }

    // Convertit une Entité JPA en DTO Response
    public CentreResponse toResponse(Centre centre) {
        if (centre == null) {
            return null;
        }
        return new CentreResponse(
                centre.getId(),
                centre.getNom(),
                centre.getDescription(),
                centre.getAdresse(),
                centre.getTelephone(),
                centre.getStatut(),
                centre.getAdministrateur() == null ? null : centre.getAdministrateur().getId()
        );
    }
}