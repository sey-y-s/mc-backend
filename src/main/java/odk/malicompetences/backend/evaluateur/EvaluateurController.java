package odk.malicompetences.backend.evaluateur;

import jakarta.validation.Valid;
import odk.malicompetences.backend.validation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/evaluateur")
@PreAuthorize("hasAnyRole('CITOYEN','EVALUATEUR','ADMIN','SUPER_ADMIN')")
public class EvaluateurController {
    private final EvaluateurService service;

    public EvaluateurController(EvaluateurService service) {
        this.service = service;
    }

    @PostMapping("/demandes")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public EvaluateurDemandeResponse creerDemande(@Valid @RequestBody ValidationRequest request, Authentication a) {
        return service.creerDemande(request, a.getName());
    }

    @GetMapping("/mes-centres")
    public List<EvaluateurCentreResponse> mesCentres(Authentication a) {
        return service.mesCentres(a.getName());
    }

    @GetMapping("/centres-disponibles")
    public List<odk.malicompetences.backend.centre.CentreResponse> centresDisponibles(
            Authentication a,
            @RequestParam(required = false) UUID citoyenCompetenceId) {
        // Avec la compétence : seuls les centres actifs, agréés et disposant d'un évaluateur habilité.
        return service.centresDisponibles(a.getName(), citoyenCompetenceId);
    }

    @GetMapping("/demandes")
    public List<EvaluateurDemandeResponse> demandes(Authentication a) {
        return service.demandes(a.getName());
    }

        @GetMapping("/historique")
    public List<EvaluateurDemandeResponse> historique(Authentication a) {
        return service.historique(a.getName());
    }

    @GetMapping("/demandes/{id}/portfolio")
    public List<odk.malicompetences.backend.portfolio.PortfolioRealisationResponse> portfolio(
            @PathVariable UUID id, Authentication a) {
        return service.portfolioDeLaDemande(id, a.getName());
    }

    @PatchMapping("/demandes/{id}/decision")
    public ValidationResponse decider(@PathVariable UUID id, @Valid @RequestBody ValidationStatutRequest r, Authentication a) {
        return service.decider(id, r.statut(), r.commentaire(), r.niveauValide(), a.getName());
    }
}
