package odk.malicompetences.backend.competence;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import odk.malicompetences.backend.common.exception.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "Compétences Citoyen", description = "Gestion des compétences déclarées par les citoyens")
@RestController
@RequestMapping("/api/v1/citoyens/{citoyenId}/competences")
public class CitoyenCompetenceController {

    private final CitoyenCompetenceService service;

    public CitoyenCompetenceController(
            CitoyenCompetenceService service
    ) {
        this.service = service;
    }

    @Operation(
            summary = "Déclarer une compétence pour un citoyen",
            description = "Ajoute une compétence du référentiel au profil d'un citoyen avec son niveau et son auto-évaluation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Compétence ajoutée avec succès au profil du citoyen",
                    content = @Content(schema = @Schema(implementation = CitoyenCompetenceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de la requête invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Citoyen ou compétence introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cette compétence est déjà déclarée pour ce citoyen",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PreAuthorize("(hasRole('CITOYEN') and #citoyenId.toString() == authentication.name) or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitoyenCompetenceResponse ajouter(
            @Parameter(description = "Identifiant UUID du citoyen", required = true)
            @PathVariable UUID citoyenId,
            @RequestBody CitoyenCompetenceRequest request
    ) {
        return service.ajouter(citoyenId, request);
    }

    @Operation(
            summary = "Lister les compétences d'un citoyen",
            description = "Récupère la liste de toutes les compétences déclarées par un citoyen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des compétences récupérée avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Citoyen introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping
    public List<CitoyenCompetenceResponse> trouverParCitoyen(
            @Parameter(description = "Identifiant UUID du citoyen", required = true)
            @PathVariable UUID citoyenId
    ) {
        return service.trouverParCitoyen(citoyenId);
    }
}
