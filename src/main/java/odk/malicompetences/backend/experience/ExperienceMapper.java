package odk.malicompetences.backend.experience;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import org.springframework.stereotype.Component;

import java.util.HashSet; // <--- L'import manquant qui corrige le rouge
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ExperienceMapper {

    public Experience toEntity(ExperienceRequest request, Citoyen citoyen, Set<CitoyenCompetence> competences) {
        if (request == null) return null;

        Experience experience = new Experience();
        experience.setCitoyen(citoyen);
        experience.setTitre(request.titre());
        experience.setDescription(request.description());
        experience.setEntreprise(request.entreprise());
        experience.setDateDebut(request.dateDebut());
        experience.setDateFin(request.dateFin());
        experience.setEnCours(request.enCours());
        if (competences != null) {
            experience.setCompetences(competences);
        }

        return experience;
    }

    public void updateEntity(ExperienceRequest request, Experience experience, Citoyen citoyen, Set<CitoyenCompetence> competences) {
        if (request != null && experience != null) {
            if (citoyen != null) {
                experience.setCitoyen(citoyen);
            }
            experience.setTitre(request.titre());
            experience.setDescription(request.description());
            experience.setEntreprise(request.entreprise());
            experience.setDateDebut(request.dateDebut());
            experience.setDateFin(request.dateFin());
            experience.setEnCours(request.enCours());
            if (competences != null) {
                experience.setCompetences(competences);
            }
        }
    }

    public ExperienceResponse toResponse(Experience experience) {
        if (experience == null) return null;

        UUID citoyenId = experience.getCitoyen() != null ? experience.getCitoyen().getId() : null;
        String citoyenNomComplet = null;

        Set<UUID> competenceIds = experience.getCompetences() != null
                ? experience.getCompetences().stream().map(CitoyenCompetence::getId).collect(Collectors.toSet())
                : new HashSet<>();

        return new ExperienceResponse(
                experience.getId(),
                citoyenId,
                citoyenNomComplet,
                experience.getTitre(),
                experience.getDescription(),
                experience.getEntreprise(),
                experience.getDateDebut(),
                experience.getDateFin(),
                experience.getEnCours(),
                competenceIds
        );
    }
}