package odk.malicompetences.backend.experience;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component public class ExperienceMapper {
    public Experience toEntity(ExperienceRequest r, Citoyen c, Set<CitoyenCompetence> competences) {
        Experience e=new Experience(); e.setCitoyen(c); appliquer(r,e); if(competences!=null)e.setCompetences(new HashSet<>(competences)); return e;
    }
    public void updateEntity(ExperienceRequest r, Experience e, Citoyen c, Set<CitoyenCompetence> competences) { if(c!=null)e.setCitoyen(c); appliquer(r,e); if(competences!=null)e.setCompetences(new HashSet<>(competences)); }
    private void appliquer(ExperienceRequest r, Experience e) { e.setTitre(r.titre()); e.setDescription(r.description()); e.setEntreprise(r.entreprise()); e.setDateDebut(r.dateDebut()); e.setDateFin(r.enCours()!=null&&r.enCours()?null:r.dateFin()); e.setEnCours(Boolean.TRUE.equals(r.enCours())); }
    public ExperienceResponse toResponse(Experience e) {
        UUID id=e.getCitoyen()==null?null:e.getCitoyen().getId();
        String nom=e.getCitoyen()==null?null:(e.getCitoyen().getPrenom()+" "+e.getCitoyen().getNom()).trim();
        Set<UUID> ids=e.getCompetences()==null?new HashSet<>():e.getCompetences().stream().map(CitoyenCompetence::getId).collect(Collectors.toSet());
        return new ExperienceResponse(e.getId(),id,nom,e.getTitre(),e.getDescription(),e.getEntreprise(),e.getDateDebut(),e.getDateFin(),e.isEnCours(),ids);
    }
}
