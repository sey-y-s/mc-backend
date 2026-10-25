package odk.malicompetences.backend.evaluateur;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.statut.StatutEvaluateur;
import odk.malicompetences.backend.common.statut.StatutHabilitation;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.validation.MethodeValidationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/admin") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')") @Transactional
public class EvaluateurAdministrationController {
 private final EvaluateurRepository evaluateurs; private final CitoyenRepository citoyens; private final HabilitationEvaluateurRepository habilitations;
 private final CompetenceRepository competences; private final MethodeValidationRepository methodes; private final CentreRepository centres; private final UtilisateurRepository utilisateurs;
 public EvaluateurAdministrationController(EvaluateurRepository e,CitoyenRepository c,HabilitationEvaluateurRepository h,CompetenceRepository co,MethodeValidationRepository m,CentreRepository ce,UtilisateurRepository u){evaluateurs=e;citoyens=c;habilitations=h;competences=co;methodes=m;centres=ce;utilisateurs=u;}
 @PostMapping("/evaluateurs") @ResponseStatus(HttpStatus.CREATED) public EvaluateurResponse creerEvaluateur(@Valid @RequestBody CreerEvaluateurRequest r){var citoyen=citoyens.findById(r.citoyenId()).orElseThrow(()->new ResourceNotFoundException("Citoyen introuvable"));if(evaluateurs.findByCitoyenId(citoyen.getId()).isPresent())throw new IllegalStateException("Ce citoyen possède déjà un dossier évaluateur.");Evaluateur e=new Evaluateur();e.setCitoyen(citoyen);e.setStatut(StatutEvaluateur.EN_ATTENTE);return EvaluateurResponse.from(evaluateurs.save(e));}
 @PatchMapping("/evaluateurs/{id}/statut") public EvaluateurResponse statutEvaluateur(@PathVariable UUID id,@Valid @RequestBody StatutEvaluateurRequest r){Evaluateur e=evaluateurs.findById(id).orElseThrow(()->new ResourceNotFoundException("Évaluateur introuvable"));e.setStatut(r.statut());return EvaluateurResponse.from(evaluateurs.save(e));}
 @PostMapping("/habilitations") @ResponseStatus(HttpStatus.CREATED) public HabilitationEvaluateurResponse creerHabilitation(@Valid @RequestBody CreerHabilitationRequest r,Authentication auth){Evaluateur e=evaluateurs.findById(r.evaluateurId()).orElseThrow(()->new ResourceNotFoundException("Évaluateur introuvable"));var competence=competences.findById(r.competenceId()).orElseThrow(()->new ResourceNotFoundException("Compétence introuvable"));var methode=methodes.findById(r.methodeId()).orElseThrow(()->new ResourceNotFoundException("Méthode introuvable"));var auteur=utilisateurs.findById(UUID.fromString(auth.getName())).orElseThrow(()->new ResourceNotFoundException("Administrateur introuvable"));if(r.dateDebut()!=null&&r.dateFin()!=null&&r.dateFin().isBefore(r.dateDebut()))throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début.");HabilitationEvaluateur h=new HabilitationEvaluateur();h.setEvaluateur(e);h.setCompetence(competence);h.setMethode(methode);h.setCentre(r.centreId()==null?null:centres.findById(r.centreId()).orElseThrow(()->new ResourceNotFoundException("Centre introuvable")));h.setAutorisePar(auteur);h.setDateDebut(r.dateDebut());h.setDateFin(r.dateFin());h.setStatut(StatutHabilitation.EN_ATTENTE);return HabilitationEvaluateurResponse.from(habilitations.save(h));}
 @PatchMapping("/habilitations/{id}/statut") public HabilitationEvaluateurResponse statutHabilitation(@PathVariable UUID id,@Valid @RequestBody StatutHabilitationRequest r){HabilitationEvaluateur h=habilitations.findById(id).orElseThrow(()->new ResourceNotFoundException("Habilitation introuvable"));h.setStatut(r.statut());return HabilitationEvaluateurResponse.from(habilitations.save(h));}
 public record CreerEvaluateurRequest(@NotNull UUID citoyenId){}
 public record StatutEvaluateurRequest(@NotNull StatutEvaluateur statut){}
 public record CreerHabilitationRequest(@NotNull UUID evaluateurId,@NotNull UUID competenceId,@NotNull UUID methodeId,UUID centreId,LocalDate dateDebut,LocalDate dateFin){}
 public record StatutHabilitationRequest(@NotNull StatutHabilitation statut){}
 public record EvaluateurResponse(UUID id,UUID citoyenId,StatutEvaluateur statut){static EvaluateurResponse from(Evaluateur e){return new EvaluateurResponse(e.getId(),e.getCitoyen().getId(),e.getStatut());}}
}
