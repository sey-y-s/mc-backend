package odk.malicompetences.backend.competence;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import odk.malicompetences.backend.common.exception.ApiError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Référentiel Compétences", description = "Consultation du référentiel national des compétences")

@RestController
@RequestMapping("/api/v1/competences")
public class CompetenceController {

    private final CompetenceService competenceService;

    public CompetenceController(CompetenceService competenceService) {
        this.competenceService = competenceService;
    }

    @Operation(
            summary = "Lister toutes les compétences",
            description = "Récupère le catalogue complet des compétences du référentiel national."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Référentiel des compétences récupéré avec succès"
            )
    })
    @GetMapping
    public List<CompetenceResponse> trouverToutes() {
        return competenceService.trouverToutes();
    }

    @Operation(
            summary = "Consulter une compétence par ID",
            description = "Récupère les détails d'une compétence du référentiel par son identifiant unique."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compétence trouvée",
                    content = @Content(schema = @Schema(implementation = CompetenceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compétence introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public CompetenceResponse trouverParId(
            @Parameter(description = "Identifiant UUID de la compétence", required = true)
            @PathVariable UUID id
    ) {
        return competenceService.trouverParId(id);
    }
}