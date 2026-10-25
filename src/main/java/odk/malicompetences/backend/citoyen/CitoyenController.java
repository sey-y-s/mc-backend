package odk.malicompetences.backend.citoyen;


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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Citoyens", description = "Gestion des profils citoyens et passeports de compétences")
@RestController
@RequestMapping("/api/v1/citoyens")
public class CitoyenController {

    private final CitoyenService citoyenService;
    private final CvService cvService;

    public CitoyenController(CitoyenService citoyenService, CvService cvService) {
        this.citoyenService = citoyenService;
        this.cvService = cvService;
    }

    @Operation(summary = "Créer mon profil citoyen", description = "Crée le profil associé au compte citoyen connecté.")
    @PreAuthorize("hasRole('CITOYEN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitoyenResponse creerMonProfil(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CitoyenRequest request
    ) {
        return citoyenService.creer(UUID.fromString(principal.getUsername()), request);
    }

    @Operation(
            summary = "Créer un profil citoyen pour un compte",
            description = "Crée un nouveau profil citoyen associé à un compte utilisateur existant."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Profil citoyen créé avec succès",
                    content = @Content(schema = @Schema(implementation = CitoyenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de la requête invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Utilisateur introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Un profil citoyen existe déjà pour cet utilisateur",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/utilisateur/{utilisateurId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CitoyenResponse creer(
            @Parameter(description = "Identifiant UUID du compte utilisateur", required = true)
            @PathVariable UUID utilisateurId,
            @Valid @RequestBody CitoyenRequest request
    ) {
        return citoyenService.creer(utilisateurId, request);
    }

    @Operation(
            summary = "Consulter un citoyen par ID",
            description = "Récupère les informations détaillées du profil d'un citoyen par son identifiant unique."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profil citoyen trouvé",
                    content = @Content(schema = @Schema(implementation = CitoyenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Citoyen introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PreAuthorize("hasRole('CITOYEN') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{id}")
    public CitoyenResponse trouverParId(
            @Parameter(description = "Identifiant UUID du citoyen", required = true)
            @PathVariable UUID id
    ) {
        return citoyenService.trouverParId(id);
    }

    @Operation(
            summary = "Mettre à jour un citoyen",
            description = "Modifie l'ensemble des informations du profil d'un citoyen."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profil citoyen mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = CitoyenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de mise à jour invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Citoyen introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CitoyenResponse> modifier(
            @Parameter(description = "Identifiant UUID du citoyen", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody CitoyenRequest request) {

        return ResponseEntity.ok(citoyenService.modifier(id, request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CITOYEN')")
    public CitoyenResponse monProfil(@AuthenticationPrincipal UserDetails principal) {
        return citoyenService.trouverParUtilisateurId(UUID.fromString(principal.getUsername()));
    }

    @GetMapping("/me/cv")
    @PreAuthorize("hasRole('CITOYEN')")
    @Operation(summary = "Télécharger le CV du citoyen connecté")
    public ResponseEntity<ByteArrayResource> telechargerCv(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CvService.GeneratedCv cv = cvService.generate(UUID.fromString(userDetails.getUsername()));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(cv.fileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(new ByteArrayResource(cv.content()));
    }


    @PutMapping("/me")
    @PreAuthorize("hasRole('CITOYEN')")
    public CitoyenResponse modifierMonProfil(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CitoyenRequest request) {
        return citoyenService.modifierMonProfil(UUID.fromString(principal.getUsername()), request);
    }

    @PutMapping("/me/consentement-gps")
    @PreAuthorize("hasRole('CITOYEN')")
    public CitoyenResponse modifierConsentementGps(@AuthenticationPrincipal UserDetails principal, @RequestBody ConsentementGpsRequest request) {
        return citoyenService.modifierConsentementGps(UUID.fromString(principal.getUsername()), request.consenti());
    }

    @PutMapping("/me/localisation")
    @PreAuthorize("hasRole('CITOYEN')")
    public CitoyenResponse modifierLocalisation(@AuthenticationPrincipal UserDetails principal, @Valid @RequestBody LocalisationProfilRequest request) {
        return citoyenService.modifierLocalisation(UUID.fromString(principal.getUsername()), request.communeId());
    }
}
