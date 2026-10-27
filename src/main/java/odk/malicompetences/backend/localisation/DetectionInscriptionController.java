package odk.malicompetences.backend.localisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import odk.malicompetences.backend.territoire.Commune;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Détection de la commune à l'inscription, avant que le compte n'existe : la position est utilisée pour
 * pré-remplir le formulaire, elle n'est ni enregistrée ni journalisée.
 */
@RestController
@RequestMapping("/api/v1/territoires/detection")
public class DetectionInscriptionController {

    private final RechercheCommuneProche recherche;
    private final LimiteurDebit limiteur = new LimiteurDebit(20, 60_000);

    public DetectionInscriptionController(RechercheCommuneProche recherche) {
        this.recherche = recherche;
    }

    public record Position(
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {
        @Override
        public String toString() {
            return "Position[masquée]";
        }
    }

    /** @param trouvee faux si aucune commune de référence n'est assez proche : le citoyen choisit lui-même. */
    public record CommuneDetectee(boolean trouvee, UUID communeId, String communeNom,
                                  UUID regionId, String regionNom, UUID cercleId, String cercleNom) {
        static CommuneDetectee aucune() {
            return new CommuneDetectee(false, null, null, null, null, null, null);
        }
    }

    @PostMapping
    @Transactional(readOnly = true)
    public CommuneDetectee detecter(@Valid @RequestBody Position position, HttpServletRequest http) {
        if (!limiteur.autoriser(http.getRemoteAddr())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Trop de demandes, réessayez dans une minute.");
        }
        return recherche.trouver(position.latitude(), position.longitude())
                .map(DetectionInscriptionController::reponse)
                .orElseGet(CommuneDetectee::aucune);
    }

    private static CommuneDetectee reponse(Commune c) {
        var region = c.getRegion();
        var cercle = c.getCercle();
        return new CommuneDetectee(true, c.getId(), c.getNom(),
                region == null ? null : region.getId(), region == null ? null : region.getNom(),
                cercle == null ? null : cercle.getId(), cercle == null ? null : cercle.getNom());
    }
}
