package odk.malicompetences.backend.skillsGap;

import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.territoire.Region;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID; // <--- C'est cet import qu'il manquait pour UUID

@Component
public class IndicateurSkillsGapMapper {

    public IndicateurSkillsGap toEntity(IndicateurSkillsGapRequest request, Region region, Competence competence) {
        if (request == null) return null;

        IndicateurSkillsGap indicateur = new IndicateurSkillsGap();
        indicateur.setRegion(region);
        indicateur.setCompetence(competence);
        indicateur.setPeriodeDebut(request.periodeDebut());
        indicateur.setPeriodeFin(request.periodeFin());
        indicateur.setDateCalcul(LocalDateTime.now());
        indicateur.setDemandeEstimee(request.demandeEstimee());
        indicateur.setOffreDisponible(request.offreDisponible());

        // Calcul automatique de l'écart : Demande - Offre
        indicateur.setEcart(request.demandeEstimee() - request.offreDisponible());

        return indicateur;
    }

    public void updateEntity(IndicateurSkillsGapRequest request, IndicateurSkillsGap indicateur, Region region, Competence competence) {
        if (request != null && indicateur != null) {
            if (region != null) {
                indicateur.setRegion(region);
            }
            if (competence != null) {
                indicateur.setCompetence(competence);
            }
            indicateur.setPeriodeDebut(request.periodeDebut());
            indicateur.setPeriodeFin(request.periodeFin());
            indicateur.setDemandeEstimee(request.demandeEstimee());
            indicateur.setOffreDisponible(request.offreDisponible());
            indicateur.setEcart(request.demandeEstimee() - request.offreDisponible());
            indicateur.setDateCalcul(LocalDateTime.now());
        }
    }

    public IndicateurSkillsGapResponse toResponse(IndicateurSkillsGap indicateur) {
        if (indicateur == null) return null;

        UUID regionId = indicateur.getRegion() != null ? indicateur.getRegion().getId() : null;
        String regionNom = indicateur.getRegion() != null ? indicateur.getRegion().getNom() : null;

        UUID competenceId = indicateur.getCompetence() != null ? indicateur.getCompetence().getId() : null;
        String competenceNom = indicateur.getCompetence() != null ? indicateur.getCompetence().getNom() : null;

        return new IndicateurSkillsGapResponse(
                indicateur.getId(),
                regionId,
                regionNom,
                competenceId,
                competenceNom,
                indicateur.getPeriodeDebut(),
                indicateur.getPeriodeFin(),
                indicateur.getDateCalcul(),
                indicateur.getDemandeEstimee(),
                indicateur.getOffreDisponible(),
                indicateur.getEcart()
        );
    }
}