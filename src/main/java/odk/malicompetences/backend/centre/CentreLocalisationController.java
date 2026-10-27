package odk.malicompetences.backend.centre;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.CommuneRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Renseigne la commune d'un centre : le moteur de validation propose ainsi aux citoyens les centres proches d'eux. */
@RestController
@RequestMapping("/api/v1/centres/{centreId}/commune")
@Tag(name = "Centres", description = "Localisation des centres")
public class CentreLocalisationController {

    public record CommuneCentreRequest(UUID communeId) {
    }

    public record CommuneCentreResponse(UUID communeId, String communeNom, String regionNom) {
    }

    private final CentreRepository centres;
    private final CommuneRepository communes;

    public CentreLocalisationController(CentreRepository centres, CommuneRepository communes) {
        this.centres = centres;
        this.communes = communes;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Commune actuelle du centre")
    public CommuneCentreResponse lire(@PathVariable UUID centreId, Authentication authentication) {
        Centre centre = centres.findById(centreId)
                .orElseThrow(() -> new ResourceNotFoundException("Centre introuvable"));
        verifierDroit(centre, authentication);
        Commune commune = centre.getCommune();
        if (commune == null) {
            return new CommuneCentreResponse(null, null, null);
        }
        return new CommuneCentreResponse(commune.getId(), commune.getNom(),
                commune.getRegion() == null ? null : commune.getRegion().getNom());
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Définit la commune du centre (administrateur national ou administrateur du centre)")
    public void definir(@PathVariable UUID centreId, @RequestBody CommuneCentreRequest request,
                        Authentication authentication) {
        Centre centre = centres.findById(centreId)
                .orElseThrow(() -> new ResourceNotFoundException("Centre introuvable"));

        verifierDroit(centre, authentication);

        if (request == null || request.communeId() == null) {
            centre.setCommune(null);
        } else {
            Commune commune = communes.findById(request.communeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Commune introuvable"));
            centre.setCommune(commune);
        }
        centres.save(centre);
    }

    private static void verifierDroit(Centre centre, Authentication authentication) {
        boolean administrateurNational = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean administrateurDuCentre = centre.getAdministrateur() != null
                && centre.getAdministrateur().getId().toString().equals(authentication.getName());
        if (!administrateurNational && !administrateurDuCentre) {
            throw new AccessDeniedException("Seul l'administrateur de ce centre ou un administrateur national peut le localiser.");
        }
    }
}
