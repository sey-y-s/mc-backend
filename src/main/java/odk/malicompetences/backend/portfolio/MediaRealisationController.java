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
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "Médias de Réalisation", description = "Gestion des médias (fichiers, images, liens) illustrant les réalisations du portfolio")
@RestController
@RequestMapping("/api/v1/portfolio-realisations/{realisationId}/medias")
public class MediaRealisationController {

    private final MediaRealisationService service;
    private final PortfolioMediaStorageService mediaStorage;

    public MediaRealisationController(
            MediaRealisationService service,
            PortfolioMediaStorageService mediaStorage
    ) {
        this.service = service;
        this.mediaStorage = mediaStorage;
    }

    @Operation(
            summary = "Ajouter un média à une réalisation",
            description = "Associe un média (image, vidéo, document, lien) à une réalisation de portfolio existante."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Média ajouté avec succès",
                    content = @Content(schema = @Schema(implementation = MediaRealisationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données du média invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Réalisation introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MediaRealisationResponse ajouter(
            @Parameter(description = "Identifiant UUID de la réalisation", required = true)
            @PathVariable UUID realisationId,
            @Valid @RequestBody MediaRealisationRequest request
    ) {
        return service.ajouter(realisationId, request);
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaRealisationResponse televerser(
            @PathVariable UUID realisationId,
            @RequestPart("fichier") MultipartFile fichier,
            @RequestParam("type") TypeMedia type,
            @RequestParam(value = "legende", required = false) String legende
    ) {
        String url = mediaStorage.store(fichier, type);
        String nom = legende == null || legende.isBlank()
                ? fichier.getOriginalFilename()
                : legende.trim();
        if (nom == null || nom.isBlank()) nom = "media";
        nom = nom.replaceAll("[\\p{Cntrl}]", "");
        if (nom.length() > 255) nom = nom.substring(0, 255);

        try {
            return service.ajouter(realisationId, new MediaRealisationRequest(type, url, nom));
        } catch (RuntimeException exception) {
            mediaStorage.delete(url);
            throw exception;
        }
    }

    @Operation(
            summary = "Lister les médias d'une réalisation",
            description = "Récupère l'ensemble des médias associés à une réalisation du portfolio."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des médias récupérée avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Réalisation introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping
    public List<MediaRealisationResponse> trouverParRealisation(
            @Parameter(description = "Identifiant UUID de la réalisation", required = true)
            @PathVariable UUID realisationId
    ) {
        return service.trouverParRealisation(realisationId);
    }

    @Operation(
            summary = "Consulter un média par ID",
            description = "Récupère les détails d'un média de réalisation par son identifiant unique."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Média trouvé",
                    content = @Content(schema = @Schema(implementation = MediaRealisationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Média introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<MediaRealisationResponse> getById(
            @Parameter(description = "Identifiant UUID du média", required = true)
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                service.getById(id)
        );
    }

    @Operation(
            summary = "Mettre à jour un média",
            description = "Modifie les informations d'un média de réalisation existant."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Média mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = MediaRealisationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de mise à jour invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Média introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<MediaRealisationResponse> modifier(
            @Parameter(description = "Identifiant UUID du média", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody MediaRealisationRequest request) {

        return ResponseEntity.ok(
                service.modifier(id, request)
        );
    }

    @Operation(
            summary = "Supprimer un média",
            description = "Supprime un média d'une réalisation du portfolio."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Média supprimé avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Média introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @Parameter(description = "Identifiant UUID du média", required = true)
            @PathVariable UUID id) {

        MediaRealisationResponse media = service.getById(id);
        service.supprimer(id);
        mediaStorage.delete(media.urlMedia());
        return ResponseEntity.noContent().build();
    }

}
