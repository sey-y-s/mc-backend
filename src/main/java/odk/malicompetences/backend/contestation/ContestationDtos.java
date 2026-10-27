package odk.malicompetences.backend.contestation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import odk.malicompetences.backend.competence.NiveauCompetence;
import odk.malicompetences.backend.validation.StatutValidation;

import java.time.LocalDateTime;
import java.util.UUID;

public final class ContestationDtos {

    private ContestationDtos() {
    }

    public record NouvelleContestation(
            @NotNull(message = "Précisez ce que vous contestez") TypeObjetContestation typeObjet,
            UUID validationId,
            @Size(max = 60) String champConcerne,
            @NotBlank(message = "Expliquez votre demande") @Size(max = 2000, message = "2000 caractères maximum") String motif
    ) {
    }

    /** Traitement par l'administration. Pour une décision acceptée : la nouvelle décision à enregistrer. */
    public record Traitement(
            @NotNull StatutContestation statut,
            @Size(max = 2000) String reponse,
            StatutValidation nouvelleDecision,
            NiveauCompetence niveauValide
    ) {
    }

    public record ContestationResponse(
            UUID id,
            UUID citoyenId,
            String citoyenNom,
            TypeObjetContestation typeObjet,
            UUID validationId,
            String competenceNom,
            StatutValidation decisionContestee,
            String champConcerne,
            String motif,
            StatutContestation statut,
            String reponse,
            UUID nouvelleDecisionId,
            StatutValidation nouvelleDecision,
            LocalDateTime dateCreation,
            LocalDateTime dateTraitement
    ) {
        static ContestationResponse from(Contestation c) {
            var v = c.getValidation();
            var nd = c.getNouvelleDecision();
            return new ContestationResponse(
                    c.getId(),
                    c.getCitoyen().getId(),
                    (c.getCitoyen().getPrenom() + " " + c.getCitoyen().getNom()).trim(),
                    c.getTypeObjet(),
                    v == null ? null : v.getId(),
                    v == null ? null : v.getCitoyenCompetence().getCompetence().getNom(),
                    v == null ? null : v.getStatut(),
                    c.getChampConcerne(),
                    c.getMotif(),
                    c.getStatut(),
                    c.getReponse(),
                    nd == null ? null : nd.getId(),
                    nd == null ? null : nd.getStatut(),
                    c.getDateCreation(),
                    c.getDateTraitement()
            );
        }
    }
}
