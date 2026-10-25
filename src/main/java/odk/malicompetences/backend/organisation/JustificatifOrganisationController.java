package odk.malicompetences.backend.organisation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisations")
public class JustificatifOrganisationController {
    private final JustificatifOrganisationService service;
    public JustificatifOrganisationController(JustificatifOrganisationService service) { this.service = service; }

    @GetMapping("/me/justificatifs") @PreAuthorize("hasRole('ORGANISATION')")
    public List<JustificatifOrganisationResponse> mesJustificatifs() { return service.mesJustificatifs(); }

    @PostMapping("/me/justificatifs") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ORGANISATION')")
    public JustificatifOrganisationResponse deposer(@Valid @RequestBody JustificatifOrganisationRequest request) { return service.deposer(request); }

    @GetMapping("/admin/{organisationId}/justificatifs") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<JustificatifOrganisationResponse> pourOrganisation(@PathVariable UUID organisationId) {
        return service.pourOrganisation(organisationId);
    }

    @PatchMapping("/admin/justificatifs/{id}") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public JustificatifOrganisationResponse examiner(@PathVariable UUID id, @Valid @RequestBody DecisionJustificatifRequest request) {
        return service.examiner(id, request.statut(), request.motif());
    }

    public record DecisionJustificatifRequest(@NotNull StatutJustificatifOrganisation statut, @Size(max = 2000) String motif) {}
}
