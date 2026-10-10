package odk.malicompetences.backend.talent;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matching")
public class TalentMatchingController {

    private final TalentRechercheService service;

    public TalentMatchingController(TalentRechercheService service) {
        this.service = service;
    }

    @GetMapping("/besoins/{besoinId}")
    public List<TalentMatchingResponse> correspondances(
            @PathVariable UUID besoinId,
            @AuthenticationPrincipal UserDetails user
    ) {
        boolean administrateur = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
        return service.correspondances(besoinId, UUID.fromString(user.getUsername()), administrateur);
    }
}
