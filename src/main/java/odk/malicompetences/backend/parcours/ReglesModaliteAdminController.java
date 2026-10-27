package odk.malicompetences.backend.parcours;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Paramétrage, par compétence, des modalités de validation autorisées (administrateurs et organismes responsables). */
@RestController
@RequestMapping("/api/v1/admin/competences/{competenceId}/modalites")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'INSTITUTION')")
@Tag(name = "Règles de validation", description = "Modalités de validation autorisées par compétence")
public class ReglesModaliteAdminController {

    private final ReglesModaliteService service;

    public ReglesModaliteAdminController(ReglesModaliteService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Règles de validation de la compétence (par défaut si rien n'est configuré)")
    public List<RegleModalite> lister(@PathVariable UUID competenceId) {
        return service.reglesPourAdmin(competenceId);
    }

    @PutMapping
    @Operation(summary = "Remplace les règles de validation de la compétence")
    public List<RegleModalite> remplacer(@PathVariable UUID competenceId,
                                         @RequestBody List<RegleModaliteRequest> regles) {
        return service.remplacer(competenceId, regles);
    }

    @DeleteMapping
    @Operation(summary = "Revient aux règles par défaut")
    public List<RegleModalite> reinitialiser(@PathVariable UUID competenceId) {
        return service.reinitialiser(competenceId);
    }
}
