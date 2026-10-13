package odk.malicompetences.backend.preuve;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('CITOYEN')")
public class CitoyenPreuveController {

    private final PreuveService preuveService;

    public CitoyenPreuveController(PreuveService preuveService) {
        this.preuveService = preuveService;
    }

    @GetMapping("/api/citoyens/me/preuves")
    public List<CitoyenPreuveResponse> listerMesPreuves(
            @AuthenticationPrincipal UserDetails principal
    ) {
        return preuveService.listerMesPreuves(UUID.fromString(principal.getUsername()));
    }

    @GetMapping("/api/citoyen-competences/{citoyenCompetenceId}/preuves")
    public List<CitoyenPreuveResponse> listerParCompetence(
            @PathVariable UUID citoyenCompetenceId
    ) {
        return preuveService.listerPourCompetenceCitoyen(citoyenCompetenceId);
    }

    @PostMapping(
            path = "/api/citoyen-competences/{citoyenCompetenceId}/preuves",
            consumes = "multipart/form-data"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public CitoyenPreuveResponse ajouterFichier(
            @PathVariable UUID citoyenCompetenceId,
            @RequestParam("type") TypePreuve type,
            @RequestPart("fichier") MultipartFile fichier
    ) {
        return preuveService.creerPreuveFichier(citoyenCompetenceId, type, fichier);
    }

    @GetMapping("/api/preuves/{id}")
    public CitoyenPreuveResponse trouverParId(@PathVariable UUID id) {
        return preuveService.trouverPourCitoyen(id);
    }
}
