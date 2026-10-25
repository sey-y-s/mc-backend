package odk.malicompetences.backend.talent;


import odk.malicompetences.backend.competence.NiveauCompetence;
import odk.malicompetences.backend.miseenrelation.ConsultationProfilService;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.organisation.StatutOrganisation;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/talents")
@PreAuthorize("hasAnyRole('ORGANISATION', 'CITOYEN')")
public class TalentRechercheController {

    private final TalentRechercheService service;
    private final ConsultationProfilService consultationProfilService;
    private final OrganisationRepository organisationRepository;

    public TalentRechercheController(TalentRechercheService service,
                                     ConsultationProfilService consultationProfilService,
                                     OrganisationRepository organisationRepository) {
        this.service = service;
        this.consultationProfilService = consultationProfilService;
        this.organisationRepository = organisationRepository;
    }

    private void verifierAccesOrganisation(Authentication authentication) {
        boolean organisation = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANISATION"));
        if (organisation) {
            var profil = organisationRepository.findByUtilisateur_Id(UUID.fromString(authentication.getName()))
                    .orElseThrow(() -> new AccessDeniedException("Aucun profil organisation n'est associé à ce compte."));
            if (profil.getStatut() != StatutOrganisation.APPROUVEE) {
                throw new AccessDeniedException("Votre organisation doit être approuvée avant de rechercher des talents.");
            }
        }
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
        verifierAccesOrganisation(authentication);
        NiveauCompetence niveau = (niveauMin == null || niveauMin.isBlank())
                ? null
                : NiveauCompetence.valueOf(niveauMin.trim().toUpperCase(Locale.ROOT));
        TalentFiltres filtres = new TalentFiltres(competence, metier, localisation, disponibilite,
                q, competenceId, metierId, regionId, communeId, niveau);
        return service.rechercher(filtres, page, size);
    }

    @GetMapping("/{id}")
    public TalentRechercheResponse trouverParId(@PathVariable UUID id,
            @RequestParam(required = false) UUID besoinId, Authentication authentication) {
        verifierAccesOrganisation(authentication);
        boolean organisation = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANISATION"));
        if (organisation) consultationProfilService.enregistrerConsultation(UUID.fromString(authentication.getName()), id, besoinId);
        return service.trouverParId(id);
    }
}
