package odk.malicompetences.backend.institution;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/v1/institution")
@PreAuthorize("hasRole('INSTITUTION')")
public class InstitutionController {
    private final InstitutionIndicateurService service;
    public InstitutionController(InstitutionIndicateurService service) { this.service = service; }

    @GetMapping("/skills-gap")
    public List<InstitutionIndicateurResponse> skillsGap(@RequestParam UUID competenceId) {
        return service.lire(competenceId);
    }

    @GetMapping("/tableau-de-bord")
    public InstitutionDashboard tableauDeBord(@RequestParam UUID competenceId) {
        List<InstitutionIndicateurResponse> rows = service.lire(competenceId);
        long visibles = rows.stream().filter(r -> "VISIBLE".equals(r.statutConfidentialite())).count();
        long masques = rows.size() - visibles;
        List<InstitutionIndicateurResponse> tensions = rows.stream().filter(r -> r.ecart() != null)
                .sorted(Comparator.comparing(InstitutionIndicateurResponse::ecart)).limit(5).toList();
        return new InstitutionDashboard(competenceId, rows.isEmpty() ? null : rows.get(0).dateCalcul(), visibles, masques, tensions);
    }

    @GetMapping("/competences")
    public List<InstitutionIndicateurService.CompetenceOption> competences() { return service.competences(); }

    @GetMapping("/skills-gap/export")
    public ResponseEntity<ByteArrayResource> exporter(@RequestParam UUID competenceId) {
        List<InstitutionIndicateurResponse> rows = service.lire(competenceId);
        StringBuilder csv = new StringBuilder("\uFEFFregionCode,regionNom,competenceId,competenceNom,offre,demande,ecart,statutConfidentialite,motifMasquage,dateCalcul\r\n");
        for (InstitutionIndicateurResponse r : rows) {
            csv.append(csv(r.regionCode())).append(',').append(csv(r.regionNom())).append(',')
                .append(csv(r.competenceId().toString())).append(',').append(csv(r.competenceNom())).append(',')
                .append(r.offre() == null ? "" : r.offre()).append(',').append(r.demande() == null ? "" : r.demande()).append(',')
                .append(r.ecart() == null ? "" : r.ecart()).append(',').append(csv(r.statutConfidentialite())).append(',')
                .append(csv(r.motifMasquage())).append(',').append(csv(r.dateCalcul().toString())).append("\r\n");
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=skills-gap-agrege.csv")
                .cacheControl(CacheControl.noStore()).body(new ByteArrayResource(csv.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private static String csv(String value) {
        if (value == null) return "";
        String safe = value.replace("\r", " ").replace("\n", " ");
        if (!safe.isEmpty() && "=+-@".indexOf(safe.charAt(0)) >= 0) safe = "'" + safe;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
    public record InstitutionDashboard(UUID competenceId, java.time.LocalDateTime dateCalcul,
                                       long cellulesVisibles, long cellulesMasquees,
                                       List<InstitutionIndicateurResponse> competencesEnTension) {}
}
