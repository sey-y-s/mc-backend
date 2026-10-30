package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.competence.NiveauCompetence;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@PreAuthorize("hasAnyRole('CITOYEN', 'ORGANISATION', 'ADMIN', 'SUPER_ADMIN')")
@RequestMapping("/api/v1/talents")
public class TalentRechercheController {

    private final TalentRechercheService service;

    public TalentRechercheController(TalentRechercheService service) {
        this.service = service;
    }

    /**
     * Recherche de talents. Paramètres textuels historiques (competence, metier, localisation, disponibilite)
     * et paramètres du mobile (q, competenceId, metierId, regionId, communeId, niveauMin, page, size).
     * Tous sont facultatifs ; page/size limitent la liste renvoyée.
     */
    @GetMapping("/recherche")
    public List<TalentRechercheResponse> rechercher(
            @RequestParam(required = false) String competence,
            @RequestParam(required = false) String metier,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) String disponibilite,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID competenceId,
            @RequestParam(required = false) UUID metierId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) UUID communeId,
            @RequestParam(required = false) String niveauMin,
            @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        NiveauCompetence niveau = (niveauMin == null || niveauMin.isBlank())
                ? null
                : NiveauCompetence.valueOf(niveauMin.trim().toUpperCase(Locale.ROOT));
        TalentFiltres filtres = new TalentFiltres(competence, metier, localisation, disponibilite,
                q, competenceId, metierId, regionId, communeId, niveau);
                return service.rechercher(filtres, page, size, citoyenConnecte(authentication), UUID.fromString(authentication.getName()));
    }

    @GetMapping("/{id}")
        public TalentRechercheResponse trouverParId(@PathVariable UUID id, Authentication authentication) {
        return service.trouverParId(id, citoyenConnecte(authentication), UUID.fromString(authentication.getName()));
    }

    /** Profil complet : réservé à une personne avec qui une mise en relation a été acceptée (ou à l'administration). */
    @GetMapping("/{id}/profil")
    public ProfilContactResponse profil(@PathVariable UUID id, Authentication authentication) {
        boolean administrateur = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        return service.profilDuContact(id, UUID.fromString(authentication.getName()), administrateur);
    }

    /** Identifiant du citoyen connecté, ou null pour une organisation / un administrateur. */
    private static UUID citoyenConnecte(Authentication authentication) {
        boolean citoyen = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CITOYEN"));
        return citoyen ? UUID.fromString(authentication.getName()) : null;
    }
}
