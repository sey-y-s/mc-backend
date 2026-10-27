package odk.malicompetences.backend.opportunite;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Opportunités locales et candidatures. */
@RestController
public class CandidatureController {

    private final CandidatureService service;

    public CandidatureController(CandidatureService service) {
        this.service = service;
    }

    /** Opportunités pour le citoyen connecté : sa commune, puis sa région, puis le reste du Mali. */
    @GetMapping("/api/v1/opportunites/pour-moi")
    @PreAuthorize("hasRole('CITOYEN')")
    public List<OpportunitePourMoiResponse> pourMoi(Authentication auth) {
        return service.pourMoi(UUID.fromString(auth.getName()));
    }

    @PostMapping("/api/v1/opportunites/{id}/candidatures")
    @PreAuthorize("hasRole('CITOYEN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CandidatureResponse postuler(@PathVariable UUID id, @Valid @RequestBody(required = false) CandidatureRequest request,
                                        Authentication auth) {
        return service.postuler(id, UUID.fromString(auth.getName()), request);
    }

    @GetMapping("/api/v1/opportunites/mes-candidatures")
    @PreAuthorize("hasRole('CITOYEN')")
    public List<CandidatureResponse> mesCandidatures(Authentication auth) {
        return service.mesCandidatures(UUID.fromString(auth.getName()));
    }

    @GetMapping("/api/v1/opportunites/{id}/candidatures")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public List<CandidatureResponse> candidatures(@PathVariable UUID id) {
        return service.candidaturesDe(id);
    }

    @PatchMapping("/api/v1/opportunites/candidatures/{candidatureId}/statut")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public CandidatureResponse changerStatut(@PathVariable UUID candidatureId, @Valid @RequestBody CandidatureStatutRequest request) {
        return service.changerStatut(candidatureId, request.statut());
    }
}
