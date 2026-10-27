package odk.malicompetences.backend.evaluateur;

import odk.malicompetences.backend.validation.Validation;
import odk.malicompetences.backend.validation.StatutValidation;
import odk.malicompetences.backend.validation.TypeValidation;
import java.time.LocalDateTime;
import java.util.UUID;

/** Vue limitée aux informations nécessaires à l'évaluation au centre. */
public record EvaluateurDemandeResponse(
        UUID id, UUID centreId, String centreNom,
        UUID citoyenId, String citoyenNom, String citoyenPrenom,
        UUID citoyenCompetenceId, String competenceNom, String niveauCompetence, String etatCompetence,
        UUID preuveId, String preuveTitre, String preuveType, String preuveDescription, String preuveUrl,
        TypeValidation type, StatutValidation statut, LocalDateTime dateDemande,
        String commentaire
) {
    static EvaluateurDemandeResponse from(Validation v) {
        var cc = v.getCitoyenCompetence();
        var citoyen = cc.getCitoyen();
        var competence = cc.getCompetence();
        var preuve = v.getPreuve();
        return new EvaluateurDemandeResponse(
                v.getId(), v.getCentre() == null ? null : v.getCentre().getId(),
                v.getCentre() == null ? null : v.getCentre().getNom(),
                citoyen.getId(), citoyen.getNom(), citoyen.getPrenom(),
                cc.getId(), competence == null ? null : competence.getNom(),
                cc.getNiveau() == null ? null : cc.getNiveau().name(),
                cc.getEtat() == null ? null : cc.getEtat().name(),
                preuve == null ? null : preuve.getId(), preuve == null ? null : preuve.getTitre(),
                preuve == null || preuve.getType() == null ? null : preuve.getType().name(),
                preuve == null ? null : preuve.getDescription(), preuve == null ? null : preuve.getFichierUrl(),
                v.getType(), v.getStatut(), v.getDateDemande(), v.getCommentaire()
        );
    }
}
