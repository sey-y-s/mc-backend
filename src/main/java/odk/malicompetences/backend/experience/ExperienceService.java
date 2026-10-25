package odk.malicompetences.backend.experience;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service @Transactional
public class ExperienceService {
    private final ExperienceRepository experienceRepository;
    private final CitoyenRepository citoyenRepository;
    private final CitoyenCompetenceRepository competenceRepository;
    private final ExperienceMapper mapper;
    public ExperienceService(ExperienceRepository experienceRepository, CitoyenRepository citoyenRepository,
                             CitoyenCompetenceRepository competenceRepository, ExperienceMapper mapper) {
        this.experienceRepository=experienceRepository; this.citoyenRepository=citoyenRepository;
        this.competenceRepository=competenceRepository; this.mapper=mapper;
    }
    public ExperienceResponse creerExperience(UUID citoyenId, ExperienceRequest request) {
        Citoyen citoyen=chargerCitoyen(citoyenId); verifierProprietaire(citoyen);
        validerDates(request);
        Set<CitoyenCompetence> competences=chargerCompetences(request.citoyenCompetenceIds(),citoyen.getId());
        return mapper.toResponse(experienceRepository.save(mapper.toEntity(request,citoyen,competences)));
    }
    @Transactional(readOnly=true) public List<ExperienceResponse> listerParCitoyen(UUID citoyenId) {
        Citoyen citoyen=chargerCitoyen(citoyenId); verifierProprietaire(citoyen);
        return experienceRepository.findByCitoyenId(citoyenId).stream().map(mapper::toResponse).toList();
    }
    @Transactional(readOnly=true) public ExperienceResponse trouverPourCitoyen(UUID id, UUID citoyenId) {
        Citoyen citoyen=chargerCitoyen(citoyenId); verifierProprietaire(citoyen);
        Experience e=experienceRepository.findById(id).filter(x->x.getCitoyen().getId().equals(citoyenId))
                .orElseThrow(()->new ResourceNotFoundException("Expérience introuvable"));
        return mapper.toResponse(e);
    }
    public ExperienceResponse modifier(UUID id, UUID citoyenId, ExperienceRequest request) {
        Citoyen citoyen=chargerCitoyen(citoyenId); verifierProprietaire(citoyen);
        if(request.citoyenId()!=null&&!request.citoyenId().equals(citoyenId)) throw new IllegalArgumentException("Le citoyen de l'expérience ne peut pas être modifié");
        validerDates(request);
        Experience e=experienceRepository.findById(id).filter(x->x.getCitoyen().getId().equals(citoyenId))
                .orElseThrow(()->new ResourceNotFoundException("Expérience introuvable"));
        Set<CitoyenCompetence> competences=request.citoyenCompetenceIds()==null?e.getCompetences():chargerCompetences(request.citoyenCompetenceIds(),citoyenId);
        mapper.updateEntity(request,e,citoyen,competences);
        return mapper.toResponse(experienceRepository.save(e));
    }
    public void supprimer(UUID id, UUID citoyenId) {
        Citoyen citoyen=chargerCitoyen(citoyenId); verifierProprietaire(citoyen);
        Experience e=experienceRepository.findById(id).filter(x->x.getCitoyen().getId().equals(citoyenId))
                .orElseThrow(()->new ResourceNotFoundException("Expérience introuvable"));
        experienceRepository.delete(e);
    }
    private Citoyen chargerCitoyen(UUID id) { return citoyenRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Citoyen introuvable")); }
    private Set<CitoyenCompetence> chargerCompetences(Set<UUID> ids, UUID citoyenId) {
        if(ids==null||ids.isEmpty()) return new HashSet<>();
        Set<CitoyenCompetence> result=new HashSet<>(competenceRepository.findAllById(ids));
        if(result.size()!=ids.size()||result.stream().anyMatch(cc->!cc.getCitoyen().getId().equals(citoyenId)))
            throw new IllegalArgumentException("Chaque compétence associée doit appartenir au même citoyen");
        return result;
    }
    private void validerDates(ExperienceRequest r) {
        if(Boolean.TRUE.equals(r.enCours())&&r.dateFin()!=null) throw new IllegalArgumentException("Une expérience en cours ne doit pas avoir de date de fin");
        if(r.dateDebut()!=null&&r.dateFin()!=null&&r.dateFin().isBefore(r.dateDebut())) throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début");
    }
    private void verifierProprietaire(Citoyen citoyen) {
        var a=SecurityContextHolder.getContext().getAuthentication();
        boolean admin=a!=null&&a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")||x.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if(!admin&&(a==null||!citoyen.getUtilisateur().getId().toString().equals(a.getName()))) throw new AccessDeniedException("Accès refusé.");
    }
}
