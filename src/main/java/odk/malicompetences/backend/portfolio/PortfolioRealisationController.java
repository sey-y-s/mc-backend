package odk.malicompetences.backend.portfolio;

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

@Tag(name = "Portfolio Réalisations", description = "Gestion du portfolio de réalisations concrètes des citoyens")
@RestController
@RequestMapping("/api/v1/citoyens/{citoyenId}/portfolio")
public class PortfolioRealisationController {

    private final PortfolioRealisationService service;

    public PortfolioRealisationController(
            PortfolioRealisationService service
    ) {
        this.service = service;
    }

    @Operation(
            summary = "Ajouter une réalisation au portfolio",
            description = "Ajoute une réalisation concrète au portfolio de réalisations d'un citoyen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Réalisation ajoutée avec succès",
                    content = @Content(schema = @Schema(implementation = PortfolioRealisationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de la réalisation invalides",
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
    public PortfolioRealisationResponse ajouter(
            @Parameter(description = "Identifiant UUID du citoyen", required = true)
            @Valid @RequestBody PortfolioRealisationRequest request
    ) {
        return service.creerPortfolio(request);
    }

    @Operation(
            summary = "Lister les réalisations d'un citoyen",
            description = "Récupère l'ensemble des réalisations du portfolio d'un citoyen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des réalisations récupérée avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Citoyen introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping
    public List<PortfolioRealisationResponse> trouverParCitoyen(
            @Parameter(description = "Identifiant UUID du citoyen", required = true)
            @PathVariable UUID citoyenId
    ) {
        return service.listerParCitoyen(citoyenId);
    }

    @Operation(
            summary = "Consulter une réalisation par ID",
            description = "Récupère les détails d'une réalisation du portfolio par son identifiant unique."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Réalisation trouvée",
                    content = @Content(schema = @Schema(implementation = PortfolioRealisationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Réalisation introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioRealisationResponse> getById(
            @Parameter(description = "Identifiant UUID de la réalisation", required = true)
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                service.getById(id)
        );
    }

    @Operation(
            summary = "Mettre à jour une réalisation",
            description = "Modifie les informations d'une réalisation de portfolio existante."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Réalisation mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = PortfolioRealisationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de mise à jour invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Réalisation introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<PortfolioRealisationResponse> modifier(
            @Parameter(description = "Identifiant UUID de la réalisation", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody PortfolioRealisationRequest request) {

        return ResponseEntity.ok(
                service.modifier(id, request)
        );
    }

    @Operation(
            summary = "Supprimer une réalisation",
            description = "Supprime une réalisation du portfolio d'un citoyen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Réalisation supprimée avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Réalisation introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @Parameter(description = "Identifiant UUID de la réalisation", required = true)
            @PathVariable UUID id) {

        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}