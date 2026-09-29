package odk.malicompetences.backend.preuve;

import odk.malicompetences.backend.competence.CitoyenCompetence;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PreuveMapper {

    public Preuve toEntity(PreuveRequest request, CitoyenCompetence citoyenCompetence) {
        if (request == null) return null;

        Preuve preuve = new Preuve();
        preuve.setCitoyenCompetence(citoyenCompetence);
        preuve.setType(request.type());
        preuve.setTitre(request.titre());
        preuve.setDescription(request.description());
        preuve.setFichierUrl(request.fichierUrl());
        preuve.setDateDepot(LocalDateTime.now());

        return preuve;
    }

    public void updateEntity(PreuveRequest request, Preuve preuve) {
        if (request != null && preuve != null) {
            preuve.setType(request.type());
            preuve.setTitre(request.titre());
            preuve.setDescription(request.description());
            preuve.setFichierUrl(request.fichierUrl());
        }
    }

    public PreuveResponse toResponse(Preuve preuve) {
        if (preuve == null) return null;

        UUID citoyenCompetenceId = preuve.getCitoyenCompetence() != null ? preuve.getCitoyenCompetence().getId() : null;

        return new PreuveResponse(
                preuve.getId(),
                citoyenCompetenceId,
                preuve.getType(),
                preuve.getTitre(),
                preuve.getDescription(),
                preuve.getFichierUrl(),
                preuve.getDateDepot()
        );
    }
}