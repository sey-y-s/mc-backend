package odk.malicompetences.backend.centreadmin;

import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.centre.CentreAdministrateurRepository;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/centre-admin") @PreAuthorize("hasRole('CENTRE_ADMIN')")
public class CentreAdminWorkspaceController {
 private final CentreRepository centres; private final CentreAdministrateurRepository affectations; private final CitoyenRepository citoyens;
 public CentreAdminWorkspaceController(CentreRepository centres,CentreAdministrateurRepository affectations,CitoyenRepository citoyens){this.centres=centres;this.affectations=affectations;this.citoyens=citoyens;}
 private Centre centre(Authentication auth){UUID id;try{id=UUID.fromString(auth.getName());}catch(Exception e){throw new org.springframework.security.access.AccessDeniedException("Identifiant utilisateur invalide.");}var a=affectations.findByUtilisateurId(id).orElseThrow(()->new ResourceNotFoundException("Aucun centre n'est associé à ce compte administrateur."));return centres.findById(a.getCentreId()).orElseThrow(()->new ResourceNotFoundException("Le centre associé à ce compte est introuvable."));}
 @GetMapping("/mon-centre") public CentreAdminCentreResponse monCentre(Authentication auth){return CentreAdminCentreResponse.from(centre(auth));}
 @GetMapping("/citoyens") public List<CitoyenCandidatEvaluateurResponse> citoyens(Authentication auth){centre(auth);return citoyens.findAll().stream().map(CitoyenCandidatEvaluateurResponse::from).toList();}
}
