package odk.malicompetences.backend.preuve;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Routes du contrat § 6.2 pour le citoyen.
 * Préfixe : /api/v1
 *
 * GET  /citoyens/me/preuves
 * GET  /citoyens/me/competences/{id}/preuves
 * POST /citoyens/me/competences/{id}/preuves           (JSON seules)
 * POST /citoyens/me/competences/{id}/preuves/fichier   (multipart)
 * GET  /citoyens/me/competences/{id}/preuves/{preuveId}
 * PATCH /citoyens/me/competences/{id}/preuves/{preuveId}
 * DELETE /citoyens/me/competences/{id}/preuves/{preuveId}
 */
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("isAuthenticated()")
public class CitoyenPreuveController {

    private final PreuveService preuveService;

    public CitoyenPreuveController(PreuveService preuveService) {
        this.preuveService = preuveService;
    }

    @GetMapping("/citoyens/me/preuves")
    @PreAuthorize("hasRole('CITOYEN')")
    public List<PreuveResponse> listerMesPreuves(
            @AuthenticationPrincipal UserDetails principal
    ) {
        return preuveService.listerMesPreuves(UUID.fromString(principal.getUsername()));
    }

    @GetMapping("/citoyens/me/competences/{citoyenCompetenceId}/preuves")
    @PreAuthorize("hasRole('CITOYEN')")
    public List<PreuveResponse> listerParCompetence(
            @PathVariable UUID citoyenCompetenceId
    ) {
        return preuveService.listerParCompetence(citoyenCompetenceId);
    }

    @PostMapping("/citoyens/me/competences/{citoyenCompetenceId}/preuves")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CITOYEN')")
    public PreuveResponse creer(
            @PathVariable UUID citoyenCompetenceId,
            @Valid @RequestBody PreuveRequest request
    ) {
        return preuveService.creer(citoyenCompetenceId, request);
    }

    @PostMapping(
            path = "/citoyens/me/competences/{citoyenCompetenceId}/preuves/fichier",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CITOYEN')")
    public PreuveResponse creerAvecFichier(
            @PathVariable UUID citoyenCompetenceId,
            @RequestPart("metadonnees") @Valid PreuveRequest request,
            @RequestPart("fichier") MultipartFile fichier
    ) {
        return preuveService.creerAvecFichier(citoyenCompetenceId, request, fichier);
    }

    @GetMapping("/citoyens/me/competences/{citoyenCompetenceId}/preuves/{preuveId}")
    @PreAuthorize("hasRole('CITOYEN')")
    public PreuveResponse trouver(
            @PathVariable UUID citoyenCompetenceId,
            @PathVariable UUID preuveId
    ) {
        return preuveService.trouver(preuveId);
    }

    @PatchMapping("/citoyens/me/competences/{citoyenCompetenceId}/preuves/{preuveId}")
    @PreAuthorize("hasRole('CITOYEN')")
    public PreuveResponse patcher(
            @PathVariable UUID citoyenCompetenceId,
            @PathVariable UUID preuveId,
            @Valid @RequestBody PreuveRequest request
    ) {
        return preuveService.patcher(preuveId, request);
    }

    @DeleteMapping("/citoyens/me/competences/{citoyenCompetenceId}/preuves/{preuveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CITOYEN')")
    public void archiver(
            @PathVariable UUID citoyenCompetenceId,
            @PathVariable UUID preuveId
    ) {
        preuveService.archiver(preuveId);
    }
}
