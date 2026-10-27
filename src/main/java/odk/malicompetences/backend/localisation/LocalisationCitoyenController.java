package odk.malicompetences.backend.localisation;

import jakarta.validation.Valid;
import odk.malicompetences.backend.localisation.LocalisationDtos.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Commune de référence du citoyen connecté : déclaration, consentement GPS, détection ponctuelle. */
@RestController
@PreAuthorize("hasRole('CITOYEN')")
@RequestMapping("/api/v1/citoyens/me/localisation")
public class LocalisationCitoyenController {

    private final LocalisationCitoyenService service;

    public LocalisationCitoyenController(LocalisationCitoyenService service) {
        this.service = service;
    }

    @GetMapping
    public MaLocalisation lire(Authentication auth) {
        return service.lire(moi(auth));
    }

    @PutMapping
    public MaLocalisation declarer(Authentication auth, @Valid @RequestBody DeclarationCommune request) {
        return service.declarer(moi(auth), request.communeId());
    }

    @PutMapping("/consentement")
    public MaLocalisation consentement(Authentication auth, @Valid @RequestBody Consentement request) {
        return service.consentement(moi(auth), request.consenti());
    }

    /** La position est utilisée pour cette seule requête puis oubliée. */
    @PostMapping("/detection")
    public Detection detecter(Authentication auth, @Valid @RequestBody PositionPonctuelle position) {
        return service.detecter(moi(auth), position);
    }

    @PostMapping("/suggestions/{id}/accepter")
    public MaLocalisation accepter(Authentication auth, @PathVariable UUID id) {
        return service.accepter(moi(auth), id);
    }

    @PostMapping("/suggestions/{id}/refuser")
    public MaLocalisation refuser(Authentication auth, @PathVariable UUID id) {
        return service.refuser(moi(auth), id);
    }

    private static UUID moi(Authentication auth) {
        try {
            return UUID.fromString(auth.getName());
        } catch (Exception e) {
            throw new AccessDeniedException("Identifiant utilisateur invalide.");
        }
    }
}
