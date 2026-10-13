package odk.malicompetences.backend.preuve;

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

@Tag(name = "Preuves", description = "Gestion des preuves appuyant les compétences des citoyens")
@RestController
@RequestMapping("/api/v1/citoyen-competences/{citoyenCompetenceId}/preuves")
public class PreuveController {

    private final PreuveService preuveService;

    public PreuveController(PreuveService preuveService) {
        this.preuveService = preuveService;
    }

    @Operation(
            summary = "Ajouter une preuve à une compétence",
            description = "Associe une preuve (attestation, diplôme, certificat, ouvrage, etc.) à une compétence déclarée par un citoyen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Preuve ajoutée avec succès",
                    content = @Content(schema = @Schema(implementation = PreuveResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de la preuve invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compétence du citoyen introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PreuveResponse ajouter(
            @Parameter(description = "Identifiant UUID de la compétence du citoyen", required = true)
            @Valid @RequestBody PreuveRequest request
    ) {
        return preuveService.creerPreuve(
                request
        );
    }

    @Operation(
            summary = "Lister les preuves d'une compétence",
            description = "Récupère toutes les preuves associées à une compétence déclarée par un citoyen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des preuves récupérée avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compétence du citoyen introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping
    public List<PreuveResponse> trouverParCompetence(
            @Parameter(description = "Identifiant UUID de la compétence du citoyen", required = true)
            @PathVariable UUID citoyenCompetenceId
    ) {
        return preuveService.listerParCitoyenCompetence(
                citoyenCompetenceId
        );
    }

    @Operation(
            summary = "Consulter une preuve par ID",
            description = "Récupère les détails d'une preuve spécifique par son identifiant unique."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Preuve trouvée",
                    content = @Content(schema = @Schema(implementation = PreuveResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Preuve introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<PreuveResponse> getById(
            @Parameter(description = "Identifiant UUID de la preuve", required = true)
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                preuveService.getById(id)
        );
    }

    @Operation(
            summary = "Mettre à jour une preuve",
            description = "Modifie les informations d'une preuve existante."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Preuve mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = PreuveResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de mise à jour invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Preuve introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<PreuveResponse> modifier(
            @Parameter(description = "Identifiant UUID de la preuve", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody PreuveRequest request) {

        return ResponseEntity.ok(
                preuveService.modifier(id, request)
        );
    }

    @Operation(
            summary = "Supprimer une preuve",
            description = "Supprime une preuve liée à une compétence."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Preuve supprimée avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Preuve introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @Parameter(description = "Identifiant UUID de la preuve", required = true)
            @PathVariable UUID id) {

        preuveService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
