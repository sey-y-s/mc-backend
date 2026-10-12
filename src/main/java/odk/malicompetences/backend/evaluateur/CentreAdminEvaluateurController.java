package odk.malicompetences.backend.evaluateur;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/centres/{centreId}/evaluateurs")
@PreAuthorize("hasRole('CENTRE_ADMIN')")
public class CentreAdminEvaluateurController {
 private final EvaluateurService service;
 public CentreAdminEvaluateurController(EvaluateurService service){this.service=service;}
 @PostMapping
 public EvaluateurCentreResponse designer(@PathVariable UUID centreId,
      @Valid @RequestBody DesignationEvaluateurRequest request, Authentication auth) {
  if (!centreId.equals(request.centreId())) throw new IllegalArgumentException("Le centre de l'URL et du corps doit être identique.");
  return service.designer(request, auth.getName());
 }
 @GetMapping
 public List<EvaluateurCentreResponse> lister(@PathVariable UUID centreId, Authentication auth){return service.listerPourCentre(centreId, auth.getName());}
 @PatchMapping("/{citoyenId}/activation")
 public EvaluateurCentreResponse modifierActivation(@PathVariable UUID centreId, @PathVariable UUID citoyenId,
      @Valid @RequestBody ModifierActivationEvaluateurRequest request, Authentication auth) {
  return service.modifierActivation(centreId, citoyenId, request.actif(), auth.getName());
 }
}
