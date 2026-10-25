package odk.malicompetences.backend.talent;


import java.util.List;
import java.util.UUID;

public record TalentMatchingResponse(
        UUID talentId,
        TalentRechercheResponse talent,
        int score,
        int points,
        String statut,
        List<String> competencesCorrespondantes,
        List<String> competencesManquantes,
        String explication
) {
}
