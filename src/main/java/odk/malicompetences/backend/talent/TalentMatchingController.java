package odk.malicompetences.backend.talent;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.organisation.StatutOrganisation;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matching")
@PreAuthorize("hasAnyRole('ORGANISATION', 'ADMIN', 'SUPER_ADMIN')")
public class TalentMatchingController {

    private final TalentRechercheService service;
    private final OrganisationRepository organisationRepository;

    public TalentMatchingController(TalentRechercheService service, OrganisationRepository organisationRepository) {
        this.service = service;
        this.organisationRepository = organisationRepository;
    }

    @GetMapping("/besoins/{besoinId}")
    public List<TalentMatchingResponse> correspondances(
            @PathVariable UUID besoinId,
            @AuthenticationPrincipal UserDetails user
    ) {
        boolean administrateur = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!administrateur) {
            var organisation = organisationRepository.findByUtilisateur_Id(UUID.fromString(user.getUsername()))
                    .orElseThrow(() -> new AccessDeniedException("Aucun profil organisation n'est associé à ce compte."));
            if (organisation.getStatut() != StatutOrganisation.APPROUVEE) {
                throw new AccessDeniedException("Votre organisation doit être approuvée pour accéder au matching.");
            }
        }
        return service.correspondances(besoinId, administrateur ? null : organisationRepository.findByUtilisateur_Id(UUID.fromString(user.getUsername())).orElseThrow(() -> new AccessDeniedException("Aucun profil organisation n'est associé à ce compte.")).getId(), administrateur);
    }
}
