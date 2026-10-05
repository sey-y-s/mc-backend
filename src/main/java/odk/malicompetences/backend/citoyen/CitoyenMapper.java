package odk.malicompetences.backend.citoyen;

import org.springframework.stereotype.Component;

@Component
public class CitoyenMapper {

    public CitoyenResponse toResponse(Citoyen citoyen) {
        return new CitoyenResponse(
                citoyen.getId(),
                citoyen.getCodePasseport(),
                citoyen.getNom(),
                citoyen.getPrenom(),
                citoyen.getSexe().name(),
                citoyen.getPhotoUrl(),
                citoyen.getDisponibilite(),
                citoyen.getCommune() != null ? citoyen.getCommune().getId() : null
        );
    }
}