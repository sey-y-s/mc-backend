package odk.malicompetences.backend.competence;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Secteur formel / informel d'une compétence du citoyen connecté. */
@RestController
@PreAuthorize("hasRole('CITOYEN')")
public class CitoyenCompetenceParcoursController {

    private final CitoyenCompetenceService service;

    public CitoyenCompetenceParcoursController(CitoyenCompetenceService service) {
        this.service = service;
    }

    @PatchMapping("/api/v1/citoyen-competences/{id}/parcours")
    public CitoyenCompetenceResponse definirParcours(
            @PathVariable UUID id,
            @Valid @RequestBody ParcoursCompetenceRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return service.definirParcours(id, UUID.fromString(principal.getUsername()), request.parcours());
    }
}
