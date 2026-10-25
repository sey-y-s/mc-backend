package odk.malicompetences.backend.citoyen;

import org.springframework.stereotype.Component;

@Component
public class CitoyenMapper {
    public CitoyenResponse toResponse(Citoyen citoyen) {
        var localisation = citoyen.getLocalisationProfil();
        return new CitoyenResponse(
                citoyen.getId(), citoyen.getIdentifiantPublic(), citoyen.getNom(), citoyen.getPrenom(),
                citoyen.getDateNaissance(), citoyen.getSexe(), citoyen.getStatutActivite(), citoyen.isProfilPublic(),
                localisation == null || localisation.getCommune() == null ? null : localisation.getCommune().getId(),
                localisation == null || localisation.getCommune() == null ? null : localisation.getCommune().getNom()
        );
    }
}
