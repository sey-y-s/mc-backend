package odk.malicompetences.backend.validation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import odk.malicompetences.backend.common.exception.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Validations", description = "Gestion et suivi des validations de compétences des citoyens")
@RestController
@PreAuthorize("hasAnyRole('EVALUATEUR', 'ADMIN', 'SUPER_ADMIN')")
@RequestMapping(
        "/api/v1/citoyen-competences/{citoyenCompetenceId}/validations"
)
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(
            ValidationService validationService
    ) {
        this.validationService = validationService;
    }

    @Operation(
            summary = "Créer une demande de validation",
            description = "Crée une validation en attente. Réservé aux évaluateurs et administrateurs ; le validateur est déterminé depuis le compte authentifié."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Demande de validation créée avec succès",
                    content = @Content(schema = @Schema(implementation = ValidationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de la demande invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compétence du citoyen ou évaluateur introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ValidationResponse creer(
            @Parameter(description = "Identifiant UUID de la compétence du citoyen", required = true)
            @Valid @RequestBody ValidationRequest request,
            Authentication authentication
    ) {
        return validationService.creerValidation(
                request, authentication.getName()
        );
    }

    @Operation(
            summary = "Lister les validations d'une compétence",
            description = "Récupère l'ensemble des validations et demandes de validation associées à une compétence déclarée."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des validations récupérée avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compétence du citoyen introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping
    public List<ValidationResponse> trouverParCompetence(
            @Parameter(description = "Identifiant UUID de la compétence du citoyen", required = true)
            @PathVariable UUID citoyenCompetenceId
    ) {
        return validationService.listerParCitoyenCompetence(
                citoyenCompetenceId
        );
    }

    @Operation(
            summary = "Consulter une validation par ID",
            description = "Récupère les détails complets d'une validation par son identifiant unique."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Validation trouvée",
                    content = @Content(schema = @Schema(implementation = ValidationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Validation introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ValidationResponse> getById(
            @Parameter(description = "Identifiant UUID de la validation", required = true)
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                validationService.getById(id)
        );
    }

    @Operation(
            summary = "Modifier le statut d'une validation",
            description = "Décide une validation en attente : APPROUVEE ou REJETEE. Une décision ne peut pas être modifiée via cet endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statut de validation mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = ValidationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de statut invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Validation introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PatchMapping("/{id}/statut")
    public ResponseEntity<ValidationResponse> modifierStatut(
            @Parameter(description = "Identifiant UUID de la validation", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody ValidationStatutRequest request) {

        return ResponseEntity.ok(
                validationService.modifierStatut(
                        id, request.statut(), request.commentaire()
                )
        );
    }
}