package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.common.pagination.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('CITOYEN')")
public class CitoyenValidationController {

    private final ValidationService validationService;

    public CitoyenValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @GetMapping("/api/v1/citoyens/me/validations")
    public PageResponse<CitoyenValidationResponse> listerMesValidations(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) StatutValidation statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.from(validationService.listerPourCitoyen(
                UUID.fromString(principal.getUsername()), statut, page, size
        ));
    }

    // Le chemin /api/v1/... est déjà servi par ValidationController (évaluateurs) :
    // le déclarer ici aussi provoquait un « Ambiguous mapping » au démarrage.
    @GetMapping("/api/v1/citoyens/me/citoyen-competences/{citoyenCompetenceId}/validations")
    public List<CitoyenValidationResponse> listerPourCompetence(
            @PathVariable UUID citoyenCompetenceId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return validationService.listerPourCompetenceCitoyen(
                citoyenCompetenceId, UUID.fromString(principal.getUsername())
        );
    }

    @GetMapping("/api/v1/validations/{id}")
    public CitoyenValidationResponse trouverParId(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return validationService.trouverPourCitoyen(
                id, UUID.fromString(principal.getUsername())
        );
    }
}
