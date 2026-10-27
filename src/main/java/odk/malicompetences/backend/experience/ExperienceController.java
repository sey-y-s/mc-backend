package odk.malicompetences.backend.experience;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "Expériences", description = "Gestion des expériences professionnelles et bénévoles des citoyens")
@RestController
@RequestMapping("/api/v1/citoyens/{citoyenId}/experiences")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(
            ExperienceService experienceService
    ) {
        this.experienceService = experienceService;
    }

    @Operation(
            summary = "Ajouter une expérience",
            description = "Ajoute une nouvelle expérience professionnelle ou bénévole au parcours d'un citoyen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Expérience ajoutée avec succès",
                    content = @Content(schema = @Schema(implementation = ExperienceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de l'expérience invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Citoyen introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExperienceResponse ajouter(
            @Parameter(description = "Identifiant UUID du citoyen", required = true)
            @PathVariable UUID citoyenId,
            @Valid @RequestBody ExperienceRequest request
    ) {
        if (!citoyenId.equals(request.citoyenId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "L'identifiant du citoyen dans l'URL doit correspondre à celui de la requête");
        }
        return experienceService.creerExperience(request);
    }

    @Operation(
            summary = "Lister les expériences d'un citoyen",
            description = "Récupère l'ensemble des expériences professionnelles et bénévoles d'un citoyen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des expériences récupérée avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Citoyen introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping
    public List<ExperienceResponse> trouverParCitoyen(
            @Parameter(description = "Identifiant UUID du citoyen", required = true)
            @PathVariable UUID citoyenId
    ) {
        return experienceService.listerParCitoyen(citoyenId);
    }

    @Operation(
            summary = "Consulter une expérience par ID",
            description = "Récupère les détails d'une expérience par son identifiant unique."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Expérience trouvée",
                    content = @Content(schema = @Schema(implementation = ExperienceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Expérience introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExperienceResponse> getById(
            @PathVariable UUID citoyenId,
            @Parameter(description = "Identifiant UUID de l'expérience", required = true)
            @PathVariable UUID id) {

        return ResponseEntity.ok(experienceService.trouverPourCitoyen(id, citoyenId));
    }

    @Operation(
            summary = "Mettre à jour une expérience",
            description = "Modifie les informations d'une expérience existante."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Expérience mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = ExperienceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de mise à jour invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Expérience introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExperienceResponse> modifier(
            @PathVariable UUID citoyenId,
            @Parameter(description = "Identifiant UUID de l'expérience", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody ExperienceRequest request) {

        return ResponseEntity.ok(
                experienceService.modifier(id, citoyenId, request)
        );
    }

    @Operation(
            summary = "Supprimer une expérience",
            description = "Supprime une expérience du parcours d'un citoyen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Expérience supprimée avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Expérience introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @PathVariable UUID citoyenId,
            @Parameter(description = "Identifiant UUID de l'expérience", required = true)
            @PathVariable UUID id) {

        experienceService.supprimer(id, citoyenId);
        return ResponseEntity.noContent().build();
    }

}
