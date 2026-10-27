package odk.malicompetences.backend.geolocalisation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Géolocalisation des citoyens et des organisations, et carte des talents d'une demande. */
@RestController
public class GeolocalisationController {

    private final GeolocalisationService service;

    public GeolocalisationController(GeolocalisationService service) {
        this.service = service;
    }

    // ----- Carte du Mali : tous les talents par localité (web) -----

    @GetMapping("/api/v1/talents/carte")
    @PreAuthorize("hasAnyRole('ORGANISATION', 'ADMIN', 'SUPER_ADMIN')")
    public CarteGeneraleResponse carteTalentsMali(
            @RequestParam(required = false) UUID competenceId,
            @RequestParam(required = false) UUID metierId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) String disponibilite,
            @RequestParam(required = false) String q
    ) {
        return service.carteGenerale(new odk.malicompetences.backend.talent.TalentFiltres(
                null, null, null, disponibilite, q, competenceId, metierId, regionId, null, null));
    }

    // Citoyen : plus aucune position n'est enregistrée. Voir LocalisationCitoyenController
    // (commune de référence, consentement, détection ponctuelle et confirmation).

    // ----- Organisation connectée (web) -----

    @GetMapping("/api/v1/organisations/me/position")
    @PreAuthorize("hasRole('ORGANISATION')")
    public PositionResponse maPositionOrganisation(Authentication auth) {
        return service.positionOrganisation(moi(auth));
    }

    @PutMapping("/api/v1/organisations/me/position")
    @PreAuthorize("hasRole('ORGANISATION')")
    public PositionResponse definirPositionOrganisation(Authentication auth, @Valid @RequestBody PositionRequest request) {
        return service.definirPositionOrganisation(moi(auth), request);
    }

    @DeleteMapping("/api/v1/organisations/me/position")
    @PreAuthorize("hasRole('ORGANISATION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void effacerPositionOrganisation(Authentication auth) {
        service.effacerPositionOrganisation(moi(auth));
    }

    // ----- Demande (besoin) : lieu et carte des talents -----

    @PutMapping("/api/v1/besoins/{id}/position")
    @PreAuthorize("hasAnyRole('ORGANISATION', 'ADMIN', 'SUPER_ADMIN')")
    public CarteTalentsResponse.LieuDemande definirLieuBesoin(@PathVariable UUID id, Authentication auth,
                                                              @Valid @RequestBody PositionRequest request) {
        return service.definirLieuBesoin(id, request, moi(auth), admin(auth));
    }

    @DeleteMapping("/api/v1/besoins/{id}/position")
    @PreAuthorize("hasAnyRole('ORGANISATION', 'ADMIN', 'SUPER_ADMIN')")
    public CarteTalentsResponse.LieuDemande effacerLieuBesoin(@PathVariable UUID id, Authentication auth) {
        return service.effacerLieuBesoin(id, moi(auth), admin(auth));
    }

    @GetMapping("/api/v1/besoins/{id}/carte-talents")
    @PreAuthorize("hasAnyRole('ORGANISATION', 'ADMIN', 'SUPER_ADMIN')")
    public CarteTalentsResponse carteTalents(@PathVariable UUID id, Authentication auth) {
        return service.carteTalents(id, moi(auth), admin(auth));
    }

    private static UUID moi(Authentication auth) {
        return UUID.fromString(auth.getName());
    }

    private static boolean admin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}
