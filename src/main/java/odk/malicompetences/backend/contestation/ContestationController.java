package odk.malicompetences.backend.contestation;

import jakarta.validation.Valid;
import odk.malicompetences.backend.contestation.ContestationDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ContestationController {

    private final ContestationService service;

    public ContestationController(ContestationService service) {
        this.service = service;
    }

    @PostMapping("/api/v1/citoyens/me/contestations")
    @PreAuthorize("hasRole('CITOYEN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ContestationResponse creer(Authentication auth, @Valid @RequestBody NouvelleContestation request) {
        return service.creer(moi(auth), request);
    }

    @GetMapping("/api/v1/citoyens/me/contestations")
    @PreAuthorize("hasRole('CITOYEN')")
    public List<ContestationResponse> mesContestations(Authentication auth) {
        return service.mesContestations(moi(auth));
    }

    @GetMapping("/api/v1/admin/contestations")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<ContestationResponse> lister(@RequestParam(defaultValue = "true") boolean enCours) {
        return service.lister(enCours);
    }

    @PatchMapping("/api/v1/admin/contestations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ContestationResponse traiter(@PathVariable UUID id, @Valid @RequestBody Traitement request, Authentication auth) {
        return service.traiter(id, request, moi(auth));
    }

    private static UUID moi(Authentication auth) {
        try {
            return UUID.fromString(auth.getName());
        } catch (Exception e) {
            throw new AccessDeniedException("Identifiant utilisateur invalide.");
        }
    }
}
