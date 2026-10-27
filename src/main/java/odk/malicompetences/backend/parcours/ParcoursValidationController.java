package odk.malicompetences.backend.parcours;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parcours-validation")
@PreAuthorize("hasRole('CITOYEN')")
@Tag(name = "Parcours de validation", description = "Moteur d'orientation des compétences")
public class ParcoursValidationController {

    private final ParcoursValidationService service;

    public ParcoursValidationController(ParcoursValidationService service) {
        this.service = service;
    }

    @GetMapping("/competences/{citoyenCompetenceId}")
    @Operation(summary = "Parcours de validation recommandé pour l'une de mes compétences")
    public ParcoursValidationResponse proposer(@PathVariable UUID citoyenCompetenceId, Authentication authentication) {
        return service.proposer(citoyenCompetenceId, UUID.fromString(authentication.getName()));
    }
}
