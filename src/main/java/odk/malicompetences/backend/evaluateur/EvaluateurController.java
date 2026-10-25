package odk.malicompetences.backend.evaluateur;

import odk.malicompetences.backend.validation.DemandeValidationResponse;
import odk.malicompetences.backend.validation.DemandeValidationService;
import odk.malicompetences.backend.validation.EvaluateurProfilResponse;
import odk.malicompetences.backend.common.statut.StatutDemandeValidation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/evaluateurs/me")
@PreAuthorize("hasRole('CITOYEN')")
public class EvaluateurController {
    private final DemandeValidationService service;

    public EvaluateurController(DemandeValidationService service) {
        this.service = service;
    }

    @GetMapping
    public EvaluateurProfilResponse profil(Authentication authentication) {
        return service.profilEvaluateur(authentication.getName());
    }

    @GetMapping("/habilitations")
    public List<HabilitationEvaluateurResponse> mesHabilitations(Authentication authentication) {
        return service.mesHabilitations(authentication.getName());
    }

    @GetMapping("/demandes-validation")
    public List<DemandeValidationResponse> demandes(@RequestParam(required = false) StatutDemandeValidation statut,
            Authentication authentication) {
        return service.demandesAssignees(authentication.getName(), statut);
    }
}
