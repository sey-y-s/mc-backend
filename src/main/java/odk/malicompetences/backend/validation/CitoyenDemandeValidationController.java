package odk.malicompetences.backend.validation;

import jakarta.validation.Valid;
import odk.malicompetences.backend.citoyen.CitoyenIdResolver;
import odk.malicompetences.backend.common.statut.MethodeValidationCode;
import odk.malicompetences.backend.common.statut.StatutDemandeValidation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/citoyens/{citoyenId}")
@PreAuthorize("hasRole('CITOYEN')")
public class CitoyenDemandeValidationController {
    private final DemandeValidationService service;
    private final CitoyenIdResolver citoyenIdResolver;

    public CitoyenDemandeValidationController(DemandeValidationService service, CitoyenIdResolver citoyenIdResolver) {
        this.service = service;
        this.citoyenIdResolver = citoyenIdResolver;
    }

    @GetMapping("/demandes-validation")
    public List<DemandeValidationResponse> mesDemandes(@PathVariable String citoyenId,
            @RequestParam(required = false) StatutDemandeValidation statut,
            @RequestParam(required = false) MethodeValidationCode methodeCode,
            Authentication authentication) {
        UUID id = citoyenIdResolver.resoudre(citoyenId, authentication);
        return service.demandesCitoyen(id, authentication.getName(), statut, methodeCode);
    }

    @GetMapping("/competences/{ccId}/demandes-validation")
    public List<DemandeValidationResponse> demandesCompetence(@PathVariable String citoyenId, @PathVariable UUID ccId,
            Authentication authentication) {
        citoyenIdResolver.resoudre(citoyenId, authentication);
        return service.demandesCitoyenCompetence(ccId, authentication.getName());
    }

    @PostMapping("/competences/{ccId}/demandes-validation")
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeValidationResponse creer(@PathVariable String citoyenId, @PathVariable UUID ccId,
            @Valid @RequestBody DemandeValidationRequest request, Authentication authentication) {
        citoyenIdResolver.resoudre(citoyenId, authentication);
        return service.creer(ccId, request, authentication.getName());
    }

    @GetMapping("/competences/{ccId}/demandes-validation/{demandeId}")
    public DemandeValidationDetailResponse detail(@PathVariable String citoyenId, @PathVariable UUID ccId,
            @PathVariable UUID demandeId, Authentication authentication) {
        citoyenIdResolver.resoudre(citoyenId, authentication);
        return service.detailCitoyen(ccId, demandeId, authentication.getName());
    }

    @PostMapping("/competences/{ccId}/demandes-validation/{demandeId}/annulation")
    public DemandeValidationResponse annuler(@PathVariable String citoyenId, @PathVariable UUID ccId,
            @PathVariable UUID demandeId, Authentication authentication) {
        citoyenIdResolver.resoudre(citoyenId, authentication);
        return service.annulerParCitoyen(ccId, demandeId, authentication.getName());
    }

    @PostMapping("/competences/{ccId}/demandes-validation/{demandeId}/complement")
    public DemandeValidationResponse complement(@PathVariable String citoyenId, @PathVariable UUID ccId,
            @PathVariable UUID demandeId, @Valid @RequestBody ComplementValidationRequest request,
            Authentication authentication) {
        citoyenIdResolver.resoudre(citoyenId, authentication);
        service.detailCitoyen(ccId, demandeId, authentication.getName());
        return service.envoyerComplement(demandeId, request.message(), authentication.getName());
    }
}
