package odk.malicompetences.backend.centreadmin;

import jakarta.validation.Valid;
import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.evaluateur.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/centre-admin")
@PreAuthorize("hasRole('CENTRE_ADMIN')")
public class CentreAdminWorkspaceController {
 private final CentreRepository centres; private final CitoyenRepository citoyens; private final EvaluateurService service;
 public CentreAdminWorkspaceController(CentreRepository centres, CitoyenRepository citoyens, EvaluateurService service) { this.centres=centres; this.citoyens=citoyens; this.service=service; }
 private Centre centre(Authentication auth) {
  UUID id; try { id=UUID.fromString(auth.getName()); } catch (Exception e) { throw new org.springframework.security.access.AccessDeniedException("Identifiant utilisateur invalide."); }
  return centres.findFirstByAdministrateurId(id).orElseThrow(() -> new ResourceNotFoundException("Aucun centre n'est associé à ce compte administrateur."));
 }
 @GetMapping("/mon-centre") public CentreAdminCentreResponse monCentre(Authentication auth) { return CentreAdminCentreResponse.from(centre(auth)); }
 @GetMapping("/citoyens") public List<CitoyenCandidatEvaluateurResponse> citoyens(Authentication auth) { centre(auth); return citoyens.findAll().stream().map(CitoyenCandidatEvaluateurResponse::from).toList(); }
 @GetMapping("/evaluateurs") public List<EvaluateurCentreResponse> evaluateurs(Authentication auth) { return service.listerPourCentre(centre(auth).getId(), auth.getName()); }
 @PostMapping("/evaluateurs") public EvaluateurCentreResponse designer(@Valid @RequestBody DesignationEvaluateurCentreRequest request, Authentication auth) {
  Centre c=centre(auth); return service.designer(new DesignationEvaluateurRequest(request.citoyenId(), c.getId(), request.specialite()), auth.getName());
 }
 @PatchMapping("/evaluateurs/{citoyenId}/activation") public EvaluateurCentreResponse modifierActivation(@PathVariable UUID citoyenId, @Valid @RequestBody ModifierActivationEvaluateurRequest request, Authentication auth) {
  return service.modifierActivation(centre(auth).getId(), citoyenId, request.actif(), auth.getName());
 }
}
