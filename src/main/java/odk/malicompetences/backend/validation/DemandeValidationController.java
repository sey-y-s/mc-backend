package odk.malicompetences.backend.validation;

import jakarta.validation.Valid;
import odk.malicompetences.backend.common.statut.MethodeValidationCode;
import odk.malicompetences.backend.common.statut.StatutDemandeValidation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/demandes-validation")
public class DemandeValidationController {
    private final DemandeValidationService service;

    public DemandeValidationController(DemandeValidationService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','CITOYEN')")
    public List<DemandeValidationResponse> rechercher(
            @RequestParam(required = false) StatutDemandeValidation statut,
            @RequestParam(required = false) MethodeValidationCode methodeCode,
            @RequestParam(required = false) UUID competenceId,
            Authentication authentication) {
        return service.demandesPersonnel(statut, methodeCode, competenceId, authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','CITOYEN')")
    public DemandeValidationDetailResponse detail(@PathVariable UUID id, Authentication authentication) {
        return service.detailPersonnel(id, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{id}/assignation")
    public DemandeValidationResponse assigner(@PathVariable UUID id, @Valid @RequestBody AssignationEvaluateurRequest request) {
        return service.assigner(id, request.evaluateurId());
    }

    @PostMapping("/{id}/decisions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','CITOYEN')")
    public DemandeValidationResponse decider(@PathVariable UUID id, @Valid @RequestBody DecisionValidationRequest request, Authentication authentication) {
        return service.decider(id, request, authentication.getName());
    }

    @GetMapping("/{id}/decisions")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','CITOYEN')")
    public List<DecisionValidationResponse> historique(@PathVariable UUID id, Authentication authentication) {
        return service.historique(id, authentication.getName());
    }

    @GetMapping("/{id}/evaluations")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','CITOYEN')")
    public List<EvaluationPratiqueResponse> evaluations(@PathVariable UUID id, Authentication authentication) {
        return service.evaluations(id, authentication.getName());
    }
}
