package odk.malicompetences.backend.evaluateur;
import jakarta.validation.Valid;
import odk.malicompetences.backend.validation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/evaluateur") @PreAuthorize("hasAnyRole('CITOYEN','EVALUATEUR','ADMIN','SUPER_ADMIN')")
public class EvaluateurController {
 private final EvaluateurService service;
 public EvaluateurController(EvaluateurService service){this.service=service;}
 @PostMapping("/demandes")
 @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
 public EvaluateurDemandeResponse creerDemande(@Valid @RequestBody ValidationRequest request, Authentication a) {
  return service.creerDemande(request, a.getName());
 }
 @GetMapping("/mes-centres") public List<EvaluateurCentreResponse> mesCentres(Authentication a){return service.mesCentres(a.getName());}
 @GetMapping("/centres-disponibles") public List<odk.malicompetences.backend.centre.CenterResponse> centresDisponibles(Authentication a){return service.centresDisponibles(a.getName());}
 @GetMapping("/demandes") public List<EvaluateurDemandeResponse> demandes(Authentication a){return service.demandes(a.getName());}
 @PatchMapping("/demandes/{id}/decision") public ValidationResponse decider(@PathVariable UUID id,@Valid @RequestBody ValidationStatutRequest r,Authentication a){return service.decider(id,r.statut(),r.commentaire(),a.getName());}
}
