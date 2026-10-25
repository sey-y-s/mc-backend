package odk.malicompetences.backend.centre;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/admin/agrements-centres") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class CentreCompetenceValidationController {
 private final CentreCompetenceValidationRepository repository; private final CentreRepository centres; private final CompetenceRepository competences;
 public CentreCompetenceValidationController(CentreCompetenceValidationRepository r,CentreRepository c,CompetenceRepository co){repository=r;centres=c;competences=co;}
 @GetMapping @Transactional(readOnly = true) public List<AgrementResponse> lister(){return repository.findAll().stream().map(AgrementResponse::from).toList();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public AgrementResponse creer(@Valid @RequestBody CreerAgrementRequest r){if(r.valideDu()!=null&&r.valideAu()!=null&&r.valideAu().isBefore(r.valideDu()))throw new IllegalArgumentException("La fin d'agrément doit être postérieure au début.");Centre centre=centres.findById(r.centreId()).orElseThrow(()->new ResourceNotFoundException("Centre introuvable"));var competence=competences.findById(r.competenceId()).orElseThrow(()->new ResourceNotFoundException("Compétence introuvable"));CentreCompetenceValidation a=new CentreCompetenceValidation();a.setCentre(centre);a.setCompetence(competence);a.setActive(true);a.setValideDu(r.valideDu());a.setValideAu(r.valideAu());return AgrementResponse.from(repository.save(a));}
 public record CreerAgrementRequest(@NotNull UUID centreId,@NotNull UUID competenceId,LocalDate valideDu,LocalDate valideAu){}
 public record AgrementResponse(UUID id,UUID centreId,String centreNom,UUID competenceId,String competenceNom,boolean actif,LocalDate valideDu,LocalDate valideAu){static AgrementResponse from(CentreCompetenceValidation a){return new AgrementResponse(a.getId(),a.getCentre().getId(),a.getCentre().getNom(),a.getCompetence().getId(),a.getCompetence().getNom(),a.isActive(),a.getValideDu(),a.getValideAu());}}
}
