package odk.malicompetences.backend.centre;

import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.evaluateur.EvaluateurCentre;
import odk.malicompetences.backend.evaluateur.EvaluateurHabilitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Contrôles de confiance des évaluations (consignes V1.2) :
 * centre ACTIF et agréé pour la compétence ; évaluateur actif, habilitation non expirée
 * et adaptée à la compétence.
 */
@Service
@Transactional(readOnly = true)
public class HabilitationService {

    private final CentreCompetenceRepository agrements;
    private final EvaluateurHabilitationRepository habilitations;

    public HabilitationService(CentreCompetenceRepository agrements, EvaluateurHabilitationRepository habilitations) {
        this.agrements = agrements;
        this.habilitations = habilitations;
    }

    public boolean centreAgree(Centre centre, Competence competence) {
        if (centre == null || competence == null || centre.getStatut() != StatutCentre.ACTIF) return false;
        return agrements.findByCentre_IdAndCompetence_Id(centre.getId(), competence.getId())
                .map(a -> a.valableLe(LocalDate.now()))
                .orElse(false);
    }

    public void verifierCentre(Centre centre, Competence competence) {
        if (centre == null) {
            throw new IllegalArgumentException("Le centre d'évaluation est obligatoire.");
        }
        if (centre.getStatut() != StatutCentre.ACTIF) {
            throw new IllegalStateException("Ce centre n'est pas actif pour le moment : choisissez un autre centre.");
        }
        if (!centreAgree(centre, competence)) {
            throw new IllegalStateException("Ce centre n'est pas agréé pour évaluer cette compétence : choisissez un autre centre.");
        }
    }

    public boolean evaluateurHabilite(EvaluateurCentre evaluateur, Competence competence) {
        if (evaluateur == null || !evaluateur.isActif()) return false;
        if (evaluateur.getDateFinHabilitation() != null && LocalDate.now().isAfter(evaluateur.getDateFinHabilitation())) {
            return false;
        }
        if (!centreAgree(evaluateur.getCentre(), competence)) return false;
        // Sans liste explicite, l'habilitation couvre les compétences agréées du centre.
        return !habilitations.existsByEvaluateurCentre_Id(evaluateur.getId())
                || habilitations.existsByEvaluateurCentre_IdAndCompetence_Id(evaluateur.getId(), competence.getId());
    }
}
